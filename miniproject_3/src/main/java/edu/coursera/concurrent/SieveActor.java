package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {

//        throw new UnsupportedOperationException();
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for(int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });
        int numPrimes = 0;
        SieveActorActor loopActor = sieveActor;
        while(loopActor != null) {
            numPrimes += loopActor.getNumLocalPrimes();
            loopActor = loopActor.next;
        }
        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        private static final int MAX_LOCAL_PRIMES = 50;
        private final int localPrimes[];
        private int numLocalPrimes = 0;
        SieveActorActor next = null;


        public SieveActorActor(int num) {
            this.numLocalPrimes = 1;
            this.localPrimes = new int[MAX_LOCAL_PRIMES];
            this.next = null;
            this.localPrimes[0] = num;
        }

        public int getNumLocalPrimes(){
            return numLocalPrimes;
        }

        @Override
        public void process(final Object msg) {
//            throw new UnsupportedOperationException();
            final int candidate = (Integer) msg;
            if(candidate <= 0) {
                if(next != null) next.send(msg);
            } else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if(locallyPrime) {
                    if(numLocalPrimes < MAX_LOCAL_PRIMES) {
                        localPrimes[numLocalPrimes++] = candidate;
                    } else if(next == null) {
                        next = new SieveActorActor(candidate);
                    } else {
                        next.send(msg);
                    }
                }
            }

        }

        public boolean isLocallyPrime(int candidate) {
            for(int i = 0; i < numLocalPrimes; ++i) {
                if(candidate % localPrimes[i] == 0) return false;
            }
            return true;
        }
    }
}
