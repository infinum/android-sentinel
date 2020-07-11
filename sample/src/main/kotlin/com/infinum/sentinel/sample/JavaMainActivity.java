package com.infinum.sentinel.sample;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinum.sentinel.Sentinel;
import com.infinum.sentinel.sample.databinding.ActivityJavaMainBinding;
import com.infinum.sentinel.ui.tools.ChuckerTool;
import com.infinum.sentinel.ui.tools.CollarTool;
import com.infinum.sentinel.ui.tools.DbInspectorTool;
import com.infinum.sentinel.ui.tools.DesignerTool;
import com.infinum.sentinel.ui.tools.GooglePlayTool;

import java.util.HashSet;
import java.util.Set;

public class JavaMainActivity extends AppCompatActivity {

    private Sentinel sentinel = null;

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
        tools.add(new DesignerTool());
        sentinel = Sentinel.watch(tools);

        viewBinding.showSentinel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sentinel != null) {
                    sentinel.show();
                }
            }
        });
    }
}
