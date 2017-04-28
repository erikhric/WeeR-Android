package sk.kebapp.weer.application;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by erikhric on 06/09/16.
 */
public class DataSender extends AsyncTask<String, Void, String> {

    private boolean connected;
    private Socket socket;
    private HeadData data;

    @Override
    protected String doInBackground(String... params) {
        Log.d("WEER", "data sender async task started");

        OutputStream out = null;
        try {
            socket = new Socket(params[0], Integer.parseInt(params[1]));
            out = new PrintStream(socket.getOutputStream(), false);

            connected = true;

            while (connected)
            {
                if (data.isRefreshedSinceLastSent()) {
                    socket.getOutputStream().write((data.toString() + "\n").getBytes());
                    socket.getOutputStream().flush();
                    data.sent();
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
            try {
                out.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return "Connection lost!";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("WEER", "data sender async task finished");
    }

    public void setData(HeadData data) {
        this.data = data;
    }
}
