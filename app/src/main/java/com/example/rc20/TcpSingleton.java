package com.example.rc20;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;
import static com.example.rc20.MainActivity.address;
import static com.example.rc20.MainActivity.port;

public class TcpSingleton
{
    private static TcpSingleton tcpInstance = null;
    private static OnActionListener _onReceiveListener = null;
    private static OnActionListener _onDisconnectListener = null;
    private static OnActionListener _onConnectListener = null;
    public static boolean atLeastOneTimeConnected = false;

    public static boolean tcpRunning = false;
    private static boolean discFromUi = false;
    private static Client client;

    private TcpSingleton()
    {
    }

    public static void Disconnect(boolean b)
    {
        discFromUi = b;
        if (client != null)
        {
            client.Disconnect();
        }
    }

    public boolean GetRunning()
    {
        return tcpRunning;
    }


    public interface OnActionListener
    {
        void onReceive(byte[] message);

        void onDisconnect();

        void onConnect();
    }

    public static TcpSingleton getInstance(String _ip, int _port, OnActionListener onReceiveListener, OnActionListener onDisconnectListener, OnActionListener onConnectListener)
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
        client = new Client(_ip, _port, tcpInstance);
        client.execute();
    }


    public void sendMsg(final char[] message)
    {
        client.sendMessage(message);
    }

    private static class Client extends AsyncTask<Void, String, Void>
    {
        private TcpSingleton _sing;
        String dstAddress;
        int dstPort;
        BufferedReader in;
        PrintWriter out;
        String incomingMessage;


        Client(String addr, int port, TcpSingleton sing)
        {
            dstAddress = addr;
            dstPort = port;
            _sing = sing;
        }

        Socket socket = null;

        @Override
        protected Void doInBackground(Void... arg0)
        {

            try
            {
                InetSocketAddress sockAdr = new InetSocketAddress(dstAddress, dstPort);
                socket = new Socket();
                int timeout = 5000;
                socket.connect(sockAdr, timeout);


                tcpRunning = true;
                atLeastOneTimeConnected = true;
                discFromUi = false;

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (tcpRunning)
                {
                    InputStream stream = socket.getInputStream();
                    byte[] data = new byte[30];
                    int count = stream.read(data);
                    _onReceiveListener.onReceive(data);


                    /*incomingMessage = in.readLine();
                    if (incomingMessage != null)
                    {
                        publishProgress(incomingMessage);
                        String s = "jono";
                    }
                    else
                    {
                        tcpRunning = false;
                    }
                    incomingMessage = null;*/
                }
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                Disconnect();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... message)
        {

            byte[] b = message[0].getBytes();
            _onReceiveListener.onReceive(b);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
        }

        private void Disconnect()
        {
            if (_onDisconnectListener != null)
            {
                _onDisconnectListener.onDisconnect();
            }

            tcpRunning = false;
            try
            {
                if (socket != null)
                {
                    socket.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            if (out != null)
            {
                out.flush();
                out.close();
            }
            /*in = null;
            out = null;*/

            if (MainActivity.GetInstance().GetAutoReconnect() && !discFromUi)
            {
                try
                {
                    Thread.sleep(5000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                client = new Client(address, port, _sing);
                client.execute();
                if (_onDisconnectListener != null)
                {
                    _onDisconnectListener.onConnect();
                }
            }
            else
            {
                tcpInstance = null;
            }
        }

        private void sendMessage(final char[] message)
        {
            if (out != null && !out.checkError())
            {
                out.println(message);
                out.flush();
                Log.d(TAG, "Sent Message: " + message);
            }
        }
    }
}