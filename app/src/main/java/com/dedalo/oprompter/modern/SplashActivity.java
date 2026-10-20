package com.dedalo.oprompter.modern;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;
    private static final String DEDALO_URL = "https://dedalointegraciones.vercel.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Configurar el TextView "Dédalo" como clickable
        TextView textViewDedalo = findViewById(R.id.textViewDedalo);
        textViewDedalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DEDALO_URL));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(SplashActivity.this, "No se pudo abrir el navegador", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Temporizador para ir a la actividad principal
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}