package com.infinum.sentinel.sample;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

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
        setEdgeToEdge();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tools.add(new CertificateTool());
        }
        Sentinel.watch(tools);

        viewBinding.showSentinel.setOnClickListener(v -> Sentinel.show());
    }

    private void setEdgeToEdge() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        handleStatusBarFlag(window);
    }

    private void handleStatusBarFlag(Window window) {
        View decorView = window.getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (isLightMode()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        decorView.setSystemUiVisibility(flags);
    }

    private boolean isLightMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode != Configuration.UI_MODE_NIGHT_YES;
    }
}
