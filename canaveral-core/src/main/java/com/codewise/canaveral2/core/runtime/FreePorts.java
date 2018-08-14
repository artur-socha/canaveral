package com.codewise.canaveral2.core.runtime;

import com.google.common.util.concurrent.Uninterruptibles;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

class FreePorts {

    static int findFreePort() {
        for (int tryCnt = 0; tryCnt < 10; tryCnt++) {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                return serverSocket.getLocalPort();
            } catch (IOException e) {
                Uninterruptibles.sleepUninterruptibly(100L, TimeUnit.MILLISECONDS);
            }
        }
        throw new IllegalStateException("Could not reserve another free port!");
    }
}
