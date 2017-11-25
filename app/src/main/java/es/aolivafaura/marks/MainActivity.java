package es.aolivafaura.marks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import es.aolivafaura.flexiblecoachmarkview.Coachmark;
import es.aolivafaura.flexiblecoachmarkview.FlexibleCoachmark;

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
                final FlexibleCoachmark<Button> coachmark = new FlexibleCoachmark<>(MainActivity.this);

                Button relatedButton = new Button(MainActivity.this);
                relatedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        coachmark.nextStep();
                    }
                });
                relatedButton.setText("Next coachmark");

                Coachmark<Button> c1 = new Coachmark<>(100, R.id.button, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT);
                Coachmark<Button> c3 = new Coachmark<>(100, R.id.button, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT);
                Coachmark<Button> c2 = new Coachmark<>(100, R.id.button5, relatedButton, Coachmark.POSITION_RIGHT, Coachmark.ALIGNMENT_TOP);
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
    }
}
