package com.vulnlab.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.lang.reflect.Method;

public class ReflectionActivity extends AppCompatActivity {

    private static final String TAG = "VulnReflection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflection);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        String className  = intent.getStringExtra("class_name");
        String methodName = intent.getStringExtra("method_name");
        String methodArg  = intent.getStringExtra("method_arg");

        if (className != null && methodName != null) {
            tvOutput.setText(invokeReflection(className, methodName, methodArg));
        } else {
            tvOutput.setText("Send 'class_name' and 'method_name' extras.\n\n"
                + "Example:\n  class_name=com.vulnlab.app.activities.AdminPanelActivity\n"
                + "  method_name=grantAdmin");
        }
    }

    private String invokeReflection(String className, String methodName, String methodArg) {
        try {
            Log.d(TAG, "[reflection] Class.forName: " + className
                + " method: " + methodName
                + " arg: " + methodArg);

            // VULN: Class.forName with attacker-controlled string
            Class<?> cls = Class.forName(className);
            Object instance = cls.newInstance();

            Method method;
            Object result;
            if (methodArg != null) {
                method = cls.getMethod(methodName, String.class);
                result = method.invoke(instance, methodArg);
            } else {
                method = cls.getMethod(methodName);
                result = method.invoke(instance);
            }

            return "Result: " + result;
        } catch (ClassNotFoundException e) {
            return "Class not found: " + className;
        } catch (NoSuchMethodException e) {
            return "Method not found: " + methodName;
        } catch (Exception e) {
            return "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
