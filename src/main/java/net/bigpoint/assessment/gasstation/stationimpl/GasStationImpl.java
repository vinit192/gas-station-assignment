package net.bigpoint.assessment.gasstation.stationimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationImpl implements GasStation{
	
	private double credit;
	private int sales;
	private int cancel_if_no_gas;
	private int cancel_if_expensive_gas;

	private HashMap<GasType, PriorityQueue<GasPump>> pumps;
	private HashMap<GasType, Double> cost;
	private AtomicInteger expensive_gas;
	private AtomicInteger no_gas;
	private AtomicInteger sales_num;
	private AtomicLong revnue;
	
	public GasStationImpl(HashMap<GasType, Double> prices) {
		this.credit = 0.0;
		this.sales = 0;
		this.cancel_if_no_gas = 0;
		this.cancel_if_expensive_gas = 0;
		this.cost = prices;
		this.pumps = new HashMap<GasType, PriorityQueue<GasPump>>();

		for (GasType gastype : GasType.values()) {
			pumps.put(gastype, new PriorityQueue<GasPump>(1, new GasPumpCompare()));

			// To have a price for every GasType that this GasStation offers.
			// Another implementation could be that buyGas and getPrice throws a
			// GasNotOfferedException for a GasType that has no price instead.
			if (!this.cost.containsKey(gastype))
				this.cost.put(gastype, 1.0);
		}
	}

	@Override
	public synchronized void addGasPump(GasPump pump) {
		pumps.get(pump.getGasType()).add(pump);
	}

	@Override
	public synchronized Collection<GasPump> getGasPumps() {
		ArrayList<GasPump> customerqueue = new ArrayList<GasPump>();
		for(GasType gasType : GasType.values()) {
			PriorityQueue<GasPump> queue = pumps.get(gasType);
			for(GasPump gasPump : queue) {
				customerqueue.add(new GasPump(gasPump.getGasType(), gasPump.getRemainingAmount()));
			}
		}
		return customerqueue;
	}

	@Override
	public synchronized double buyGas(GasType gasType, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {
		
		expensive_gas = new AtomicInteger(0);
		sales_num = new AtomicInteger(0);
		revnue = new AtomicLong(0);
		no_gas = new AtomicInteger(0);
		double price = 0;
		
		if (amountInLiters <= 0 || maxPricePerLiter <= 0) {
			return 0;
		}
		
		if (cost.get(gasType) > maxPricePerLiter) {
			cancel_if_expensive_gas = expensive_gas.addAndGet(1);
			throw new GasTooExpensiveException();
		}
		
		PriorityQueue<GasPump> pumptype = pumps.get(gasType);
		
		for (GasPump gaspump : pumptype) {

			if (gaspump.getGasType().equals(gasType)) {

				synchronized (gaspump) {

					if (gaspump.getRemainingAmount() >= amountInLiters) {
						gaspump.pumpGas(amountInLiters);
						price = amountInLiters * cost.get(gasType);
					/*	System.out
								.println("[PUMP STATISTICS] amount remaining: "
										+ _gaspump.getRemainingAmount());*/
						credit = revnue
								.addAndGet(new Double(price).longValue());
						sales = sales_num.addAndGet(1);
						break;
					}
				}
			}
		}
		
		if (price == 0 && amountInLiters > 0) {
			cancel_if_no_gas = no_gas.addAndGet(1);
			throw new NotEnoughGasException();
		}
		
		return price;
	}

	@Override
	public synchronized double getRevenue() {
		return credit;
	}

	@Override
	public synchronized int getNumberOfSales() {
		return sales;
	}

	@Override
	public synchronized  int getNumberOfCancellationsNoGas() {
		return cancel_if_no_gas;
	}

	@Override
	public synchronized int getNumberOfCancellationsTooExpensive() {
		return cancel_if_expensive_gas;
	}

	@Override
	public synchronized double getPrice(GasType gasType) {
		return cost.get(gasType);
	}

	@Override
	public synchronized void setPrice(GasType gasType, double price) {
		if (price > 0)
			cost.put(gasType, price);
	}
}
