package io.github.some_example_name;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;

public class InputRecorder extends InputAdapter {
    private Array<InputRecord> records = new Array<>();
    private float timer = 0f;
    private boolean recording = false;

    public void startRecording() {
        records.clear();
        timer = 0f;
        recording = true;
    }

    public void stopRecording() {
        recording = false;
    }

    public void update(float delta) {
        if (recording) timer +=delta;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (recording) {
            records.add(new InputRecord(timer, keycode, true));
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (recording) {
            records.add(new InputRecord(timer, keycode, false));
        }
        return false;
    }

    public Array<InputRecord> getRecords() {
        return records;
    }

    public float getTimer() {
        return timer;
    }
}
