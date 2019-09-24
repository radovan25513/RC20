package com.example.rc20;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TcpObject
{
    private IOnActionOnTcp _onReceiveListener;
    private IOnActionOnTcp _onDisconnectListener;
    private IOnActionOnTcp _onConnectListener;

    private boolean tcpRunning = false;
    private boolean discFromUi = false;
    private TcpClient tcpClient;

    private SharedPreferences sharedPreferences;

    private Context _context;

    private Handler _mHandler;

    TcpObject(IOnActionOnTcp onReceiveListener, IOnActionOnTcp onDisconnectListener, IOnActionOnTcp onConnectListener, Handler mHandler, Context context)
    {
        _onReceiveListener = onReceiveListener;
        _onDisconnectListener = onDisconnectListener;
        _onConnectListener = onConnectListener;
        _mHandler = mHandler;
        _context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public void initContext(Context context)
    {
        _context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public void Disconnect(boolean b)
    {
        discFromUi = b;
        if (tcpClient != null)
        {
            tcpClient.Disconnect();
        }
        else
        {
            tcpRunning = false;
            _onDisconnectListener.onDisconnect();
        }
    }

    public boolean IsRunning()
    {
        return tcpRunning;
    }


    public interface IOnActionOnTcp
    {
        void onReceive(byte[] message);

        void onDisconnect();

        void onConnect();
    }

//     TcpObject getInstance(IOnActionOnTcp onReceiveListener, IOnActionOnTcp onDisconnectListener, IOnActionOnTcp onConnectListener, Handler mHandler)
//    {
//        if (tcpInstance == null)
//        {
//            tcpInstance = new TcpObject();
//        }
//        _onReceiveListener = onReceiveListener;
//        _onDisconnectListener = onDisconnectListener;
//        _onConnectListener = onConnectListener;
//        _mHandler = mHandler;
//
//        return tcpInstance;
//    }

    public void Connect(String _ip, int _port, boolean reconnect)
    {
        tcpClient = new TcpClient(_ip, _port);
        tcpClient.execute(reconnect);
    }


    public void sendMsg(final byte[] message)
    {
        tcpClient.sendMessage(message);
    }

    private class TcpClient extends AsyncTask<Boolean, String, Void>
    {
        String dstAddress;
        int dstPort;
        DataOutputStream out;
        DataInputStream in;
        Socket socket;


        TcpClient(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;
            socket = null;
            in = null;
            out = null;
        }


        @Override
        protected Void doInBackground(Boolean... booleans)
        {
            try
            {
                Boolean isReconnected = false;
                isReconnected = booleans[0];

                if (isReconnected)
                {
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }


                InetSocketAddress sockAdr = new InetSocketAddress(dstAddress, dstPort);
                socket = new Socket();
                int timeout = 7000;
                socket.connect(sockAdr, timeout);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                tcpRunning = true;
                discFromUi = false;

                if (_onConnectListener != null)
                {
                    _onConnectListener.onConnect();
                    Message message = _mHandler.obtainMessage(1);
                    message.sendToTarget();
                }


                while (tcpRunning)
                {
                    byte[] data = new byte[30];
                    int count = in.read(data);
                    _onReceiveListener.onReceive(data);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (tcpClient != null)
                {
                    tcpClient.cancel(true);
                }

                Disconnect();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... message)
        {
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
        }

        private void Disconnect()
        {
            try
            {
                tcpRunning = false;


                if (in != null)
                {
                    in.close();
                    in = null;
                }

                if (out != null)
                {
                    out.flush();
                    out.close();
                    out = null;
                }

                if (socket != null)
                {
                    socket.close();
                    socket = null;
                }

                if (_onDisconnectListener != null)
                {
                    _onDisconnectListener.onDisconnect();
                    Message message = _mHandler.obtainMessage(0);
                    message.sendToTarget();
                }

                if (tcpClient != null) tcpClient.cancel(true);

                if (sharedPreferences.getBoolean("switch_autoconnect", true) && !discFromUi)
                {
                    tcpClient = null;
                    //connectWithSleep();
                    //Thread.sleep(4000);
                    Connect(Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187")), Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750"))), true);
                }
                else
                {
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private void sendMessage(final byte[] message)
        {
            if (out != null)
            {
                try
                {
                    out.writeInt(message.length); // write length of the message
                    out.write(message);           // write the message
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // Disconnect();
            }
        }
    }
}