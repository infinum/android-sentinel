package com.infinum.sentinel.sample;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinum.sentinel.Sentinel;
import com.infinum.sentinel.sample.databinding.ActivityJavaMainBinding;
import com.infinum.sentinel.ui.tools.CertificateTool;
import com.infinum.sentinel.ui.tools.ChuckerTool;
import com.infinum.sentinel.ui.tools.CollarTool;
import com.infinum.sentinel.ui.tools.DbInspectorTool;
import com.infinum.sentinel.ui.tools.GooglePlayTool;
import com.infinum.sentinel.ui.tools.ThimbleTool;
import com.infinum.sentinel.ui.tools.TimberTool;

import java.util.HashSet;
import java.util.Set;

public class JavaMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityJavaMainBinding viewBinding = ActivityJavaMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        final Set<Sentinel.Tool> tools = new HashSet<>();
        tools.add(new ChuckerTool());
        tools.add(new CollarTool());
        tools.add(new DbInspectorTool());
        tools.add(new GooglePlayTool());
        tools.add(new ThimbleTool());
        tools.add(new TimberTool());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            tools.add(new CertificateTool());
        }
        Sentinel.watch(tools);

        viewBinding.showSentinel.setOnClickListener(v -> Sentinel.show());
    }
}
