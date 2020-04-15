package cn.asiontang;


import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Created by Stardust on 2017/1/20.
 * <p>
 * 来自网络~~
 */
public class ProcessShell
{
    private static final String TAG = "ProcessShell";

    public static class Result
    {
        public int code = -1;
        public String error;
        public String result;

        @Override
        public String toString()
        {
            return "ShellResult{" +
                    "code=" + code +
                    ", error='" + error + '\'' +
                    ", result='" + result + '\'' +
                    '}';
        }
    }

    public static int getProcessPid(Process process)
    {
        try
        {
            Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            return (int) pid.get(process);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    protected static final String COMMAND_SU = "su";
    protected static final String COMMAND_SH = "sh";
    protected static final String COMMAND_EXIT = "exit\n";
    protected static final String COMMAND_LINE_END = "\n";

    public static Result execCommand(String[] commands, boolean isRoot)
    {
        Result commandResult = new Result();
        if (commands == null || commands.length == 0)
            throw new IllegalArgumentException("command is empty");
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands)
            {
                if (command != null)
                {
                    os.write(command.getBytes());
                    os.writeBytes(COMMAND_LINE_END);
                    os.flush();
                }
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            Log.d(TAG, "pid = " + getProcessPid(process));
            commandResult.code = process.waitFor();
            commandResult.result = readAll(process.getInputStream());
            commandResult.error = readAll(process.getErrorStream());
            Log.d(TAG, commandResult.toString());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (os != null) os.close();
                if (process != null)
                {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                }
            }
            catch (IOException ignored)
            {

            }
            if (process != null)
            {
                process.destroy();
            }
        }
        return commandResult;
    }

    private static String readAll(InputStream inputStream) throws IOException
    {
        String line;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = reader.readLine()) != null)
        {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    public static Result execCommand(String command, boolean isRoot)
    {
        String[] commands = command.split("\n");
        return execCommand(commands, isRoot);
    }
}