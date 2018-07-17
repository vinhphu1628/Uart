package com.example.xfoodz.uartexample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String UART_DEVICE_NAME = "UART0";
    private UartDevice mDevice;
    private static final int CHUNK_SIZE = 512;
    private static final int interval = 2000;
    private boolean st = false;
    private Handler mInputHandler;

    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Waiting for data...");
            transferUartData(mDevice);
            if(st) {
                st = false;
                mInputHandler.postDelayed(mTransferUartRunnable, interval);
            }
            else mInputHandler.post(mTransferUartRunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Attempt to access the UART device
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            mDevice = manager.openUartDevice(UART_DEVICE_NAME);
            configureUartFrame(mDevice);
            mInputHandler = new Handler();
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close UART device", e);
            }
        }
    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(115200);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

    private void transferUartData(UartDevice uart) {
        if (uart != null) {
            // Loop until there is no more data in the RX buffer.
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = uart.read(buffer, buffer.length)) > 0) {
                    st = true;
                    String str = new String(buffer);
                    Log.d(TAG, "Received " + str.substring(0,read) + "\n Looping data back...");
                    uart.write(buffer, read);
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
        }
    }
}
