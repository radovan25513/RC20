package com.example.rc20;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public class TcpSingleton
{
    private static TcpSingleton tcpInstance = null;
    private static IOnActionOnTcp _onReceiveListener = null;
    private static IOnActionOnTcp _onDisconnectListener = null;
    private static IOnActionOnTcp _onConnectListener = null;

    public static boolean tcpRunning = false;
    private static boolean discFromUi = false;
    private static TcpClient tcpClient;

    private static SharedPreferences sharedPreferences;

    private static Context _context;

    private TcpSingleton()
    {
        tcpInstance = this;
    }

    public static void initContext(Context context)
    {
        _context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public static void Disconnect(boolean b)
    {
        discFromUi = b;
        if (tcpClient != null)
        {
            tcpClient.Disconnect();
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

    static TcpSingleton getInstance(IOnActionOnTcp onReceiveListener, IOnActionOnTcp onDisconnectListener, IOnActionOnTcp onConnectListener)
    {
        if (tcpInstance == null)
        {
            tcpInstance = new TcpSingleton();
        }
        _onReceiveListener = onReceiveListener;
        _onDisconnectListener = onDisconnectListener;
        _onConnectListener = onConnectListener;

        return tcpInstance;
    }

    public static void Connect(String _ip, int _port)
    {
        tcpClient = new TcpClient(_ip, _port);
        tcpClient.execute();
    }


    public void sendMsg(final byte[] message)
    {
        tcpClient.sendMessage(message);
    }

    private static class TcpClient extends AsyncTask<Void, String, Void>
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
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                InetSocketAddress sockAdr = new InetSocketAddress(dstAddress, dstPort);
                socket = new Socket();
                int timeout = 1000;
                socket.connect(sockAdr, timeout);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                tcpRunning = true;
                discFromUi = false;

                _onConnectListener.onConnect();

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

                if (socket != null)
                {
                    socket.close();
                }


                if (out != null)
                {
                    out.flush();
                    out.close();
                }

                if (_onDisconnectListener != null)
                {
                    _onDisconnectListener.onDisconnect();
                }

                if (sharedPreferences.getBoolean("switch_autoconnect", true) && !discFromUi)
                {
                    Thread.sleep(4000);
                    Connect(Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187")), Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750"))));
                }
                else
                {
                    //tcpInstance = null;
                    tcpClient = null;
                }
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();

                Connect(Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187")), Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750"))));
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
        }
    }
}