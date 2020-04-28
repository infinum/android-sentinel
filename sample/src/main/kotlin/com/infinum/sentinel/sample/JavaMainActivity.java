package com.infinum.sentinel.sample;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinum.sentinel.Sentinel;
import com.infinum.sentinel.sample.databinding.ActivityJavaMainBinding;
import com.infinum.sentinel.sample.tools.SentinelTools;

public class JavaMainActivity extends AppCompatActivity {

    private Sentinel sentinel = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityJavaMainBinding viewBinding = ActivityJavaMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sentinel = Sentinel.watch(this, SentinelTools.INSTANCE.get());

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
