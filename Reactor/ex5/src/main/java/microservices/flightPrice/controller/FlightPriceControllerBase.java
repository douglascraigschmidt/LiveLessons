public class FlightPriceControllerBase {
    /**
     * A proxy to the SWA price database.
     */
    private SWAPriceProxy mSWAPriceProxy;

    /**
     * A proxy to the AA price database.
     */
    private AAPriceProxy mAAPriceProxy;

    /**
     * A helper class that holds a PriceProxy and a
     * factory for creating a PriceProxy.
     */
    private static class Tuple {
        /**
         * A PricyProxy that references an airline price
         * microservices.
         */
        PriceProxy mProxy;

        /**
         * A factory that creates a new PriceProxy.
         */
        Supplier<PriceProxy> mFactory;

        /**
         * The constructor initializes the fields.
         */
        Tuple(Supplier<PriceProxy> proxySupplier) {
            mProxy = null;
            mFactory = proxySupplier;
        }
    }

    /**
     * A list of PriceProxy objects to airline price microservices.
     */
    List<Tuple> mProxyList =
        new ArrayList<Tuple>() { {
            add(new Tuple(SWAPriceProxy::new));
            add(new Tuple(AAPriceProxy::new));
    } };

    /**
     * Initialize the flight proxies if they haven't already been
     * initialized in an earlier call.
     */
    private void initializeProxiesIfNecessary() {
        // Iterate through all the airline proxies.
        for (Tuple tuple : mProxyList)
            // If a proxy hasn't been initialized yet then initialize
            // it so it will be cached for future calls.
            if (tuple.mProxy == null)
                tuple.mProxy = tuple.mFactory.get();
    }
}
