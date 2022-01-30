package net.bigpoint.assesment.gasstation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.stationimpl.GasStationImpl;

public class GasStationImplementationTest {
	
	private GasStationImpl stationImpl;
	private GasPump gasPump;
	private GasPump pump_amout_low;
	private HashMap<GasType,Double> costs;
	private static final double DIESEL_PRICE = 1.4;

	@Before
	public void setUp() throws Exception {
		costs = new HashMap<GasType,Double>();
		costs.put(GasType.DIESEL, DIESEL_PRICE);
		stationImpl = new GasStationImpl(costs);
		gasPump = new GasPump(GasType.DIESEL, 50);
		pump_amout_low = new GasPump(GasType.SUPER, 30);
		stationImpl.addGasPump(gasPump);
	}

	@Test
	public void testAddGasPump() {
		assertTrue(stationImpl.getGasPumps().size() == 1);
		stationImpl.addGasPump(new GasPump(GasType.REGULAR, 1));
		assertTrue(stationImpl.getGasPumps().size() == 2);
	}

	@Test
	public void testAddGasPumpToReturnedCollection() {
		Collection<GasPump> pumps = stationImpl.getGasPumps();
		pumps.add(pump_amout_low);
		assertFalse(stationImpl.getGasPumps().contains(pump_amout_low));
	}

	@Test
	public void testBuyGas() throws Exception {
		double amount = 10;
		assertEquals(amount*DIESEL_PRICE, stationImpl.buyGas(GasType.DIESEL, amount, 1.5), 0);
	}

	@Test(expected=GasTooExpensiveException.class)
	public void testBuyGasMaxPriceTooLow() throws NotEnoughGasException, GasTooExpensiveException {
		stationImpl.buyGas(GasType.DIESEL, 10, 1.3);
	}

	@Test(expected=NotEnoughGasException.class)
	public void testBuyGasNotEnoughGas() throws NotEnoughGasException, GasTooExpensiveException {
		stationImpl.buyGas(GasType.DIESEL, 60, 1.5);
	}

	@Test
	public void testBuyGasInvalidArguments() throws Exception {
		assertEquals(0, stationImpl.buyGas(GasType.DIESEL, -1.0, 1.5), 0);
		assertEquals(0, stationImpl.buyGas(GasType.DIESEL, 10, -1.0), 0);
		assertEquals(0, stationImpl.buyGas(GasType.DIESEL, -10, -1.0), 0);
	}

	@Test
	public void testGetRevenue() throws Exception {
		assertEquals(0, stationImpl.getRevenue(), 0);
		stationImpl.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, stationImpl.getRevenue(), 0);
		stationImpl.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, stationImpl.getRevenue(), 0);
		stationImpl.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(10*costs.get(GasType.DIESEL), stationImpl.getRevenue(), 0);
	}

	@Test
	public void testGetNumberOfSales() throws Exception {
		assertEquals(0, stationImpl.getNumberOfSales());
		stationImpl.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, stationImpl.getNumberOfSales());
		stationImpl.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, stationImpl.getNumberOfSales());
		try {
			stationImpl.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.			
		}
		assertEquals(0, stationImpl.getNumberOfSales());
		try {
			stationImpl.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(0, stationImpl.getNumberOfSales());
		stationImpl.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(1, stationImpl.getNumberOfSales());
	}

	@Test
	public void testGetNumberOfCancellationsNoGas() throws Exception {
		assertEquals(0, stationImpl.getNumberOfCancellationsNoGas());
		stationImpl.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsNoGas());
		stationImpl.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsNoGas());
		stationImpl.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsNoGas());
		try {
			stationImpl.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(0, stationImpl.getNumberOfCancellationsNoGas());
		try {
			stationImpl.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.
		}
		assertEquals(1, stationImpl.getNumberOfCancellationsNoGas());
	}

	@Test
	public void testGetNumberOfCancellationsTooExpensive() throws Exception {
		assertEquals(0, stationImpl.getNumberOfCancellationsTooExpensive());
		stationImpl.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsTooExpensive());
		stationImpl.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsTooExpensive());
		stationImpl.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(0, stationImpl.getNumberOfCancellationsTooExpensive());
		try {
			stationImpl.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.
		}
		assertEquals(0, stationImpl.getNumberOfCancellationsTooExpensive());
		try {
			stationImpl.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(1, stationImpl.getNumberOfCancellationsTooExpensive());
	}

	@Test
	public void testGetSetPrice() {
		assertEquals(1.4, stationImpl.getPrice(GasType.DIESEL), 0);
		assertEquals(1.0, stationImpl.getPrice(GasType.REGULAR), 0);
		stationImpl.setPrice(GasType.REGULAR, -1.5);
		assertEquals(1.0, stationImpl.getPrice(GasType.REGULAR), 0);
		stationImpl.setPrice(GasType.REGULAR, 1.5);
		assertEquals(1.5, stationImpl.getPrice(GasType.REGULAR), 0);
	}

}
