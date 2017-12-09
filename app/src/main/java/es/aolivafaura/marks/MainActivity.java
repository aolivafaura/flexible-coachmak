package es.aolivafaura.marks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import es.aolivafaura.flexiblecoachmarkview.Coachmark;
import es.aolivafaura.flexiblecoachmarkview.FlexibleCoachmark;
import es.aolivafaura.flexiblecoachmarkview.GenerateView;

/**
 * Created by antonio on 11/10/17.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FlexibleCoachmark coachmark = new FlexibleCoachmark(MainActivity.this);

                Button relatedButton = new Button(MainActivity.this);
                relatedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        coachmark.nextStep();
                    }
                });
                relatedButton.setText("Next coachmark");

                Coachmark<Button> c1 = new Coachmark<>(R.id.button, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT);
                c1.setSpotDiameterDp(100);
                Coachmark<Button> c2 = new Coachmark<>(R.id.button2, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_LEFT);
                c2.setSpotDiameterPercetage(200);
                Coachmark<Button> c3 = new Coachmark<>(R.id.button5, relatedButton, Coachmark.POSITION_RIGHT, Coachmark.ALIGNMENT_TOP);
                c3.setSpotDiameterPercetage(50);
                List<Coachmark<Button>> buttonList = new ArrayList<>();
                buttonList.add(c1);
                buttonList.add(c2);
                buttonList.add(c3);
                coachmark.setSteps(buttonList);

                coachmark.setDismissListener(new FlexibleCoachmark.OnCoackmarkDismissedListener() {
                    @Override
                    public void onCoachmarkDismissed() {
                        Log.d("TETE", "DISMISSED");
                    }
                });

                coachmark.setInitialDelay(1000);
                coachmark.show();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FlexibleCoachmark coachmark = new FlexibleCoachmark(MainActivity.this);

                ViewGroup viewg = new GenerateView(MainActivity.this, R.layout.view_simple_layout)
                        .withButton(R.id.button9, R.string.title_activity_main, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                coachmark.nextStep();
                            }
                        })
                        .withText(R.id.textView, R.string.title_activity_main)
                        .generate();


                Coachmark<ViewGroup> c1 = new Coachmark<>(R.id.button, viewg, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT);
                c1.setSpotDiameterDp(100);
                Coachmark<ViewGroup> c2 = new Coachmark<>(R.id.button, viewg, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT);
                c2.setSpotDiameterPercetage(200);
                Coachmark<ViewGroup> c3 = new Coachmark<>(R.id.button5, viewg, Coachmark.POSITION_RIGHT, Coachmark.ALIGNMENT_TOP);
                c3.setSpotDiameterPercetage(50);

                List<Coachmark<ViewGroup>> buttonList = new ArrayList<>();
                buttonList.add(c1);
                buttonList.add(c2);
                buttonList.add(c3);
                coachmark.setSteps(buttonList);

                coachmark.setDismissListener(new FlexibleCoachmark.OnCoackmarkDismissedListener() {
                    @Override
                    public void onCoachmarkDismissed() {
                        Log.d("TETE", "DISMISSED");
                    }
                });

                coachmark.setInitialDelay(1000);
                coachmark.show();
            }
        });
    }
}
