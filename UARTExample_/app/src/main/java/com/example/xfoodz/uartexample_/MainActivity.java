package com.example.xfoodz.uartexample_;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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
            Log.d(TAG, "Sending data...");
            String str = "OhYeeea";
            try {
                writeUartData(mDevice, str.getBytes());
            }
            catch(IOException e){
                Log.e(TAG, "Unable to write data", e);
            }
            for(int i = 0; i < 10000; i++) {
                try {
                    readUartBuffer(mDevice);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to read buffer");
                }
                if (st) {
                    st = false;
                    Log.d(TAG, "Received data");
                    break;
                } else Log.d(TAG, "No looped data");
            }
            mInputHandler.postDelayed(mTransferUartRunnable, interval);
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

    public void writeUartData(UartDevice uart, byte[] data) throws IOException {
        byte[] buffer = data;
        int count = uart.write(buffer, buffer.length);
        Log.d(TAG, "Wrote " + count + " bytes to peripheral");
    }

    public void readUartBuffer(UartDevice uart) throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];
        int count;
        while ((count = uart.read(buffer, buffer.length)) > 0) {
            Log.d(TAG, "Read " + count + " bytes from peripheral");
            String str = new String(buffer);
            Log.d(TAG, str.substring(0,count));
            st = true;
        }
    }
}
