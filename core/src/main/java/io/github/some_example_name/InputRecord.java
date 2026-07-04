package io.github.some_example_name;

public class InputRecord {
    public float timestamp;
    public int keycode;
    public boolean pressed;

    public InputRecord(float timestamp, int keycode, boolean pressed) {
        this.timestamp = timestamp;
        this.keycode = keycode;
        this.pressed = pressed;
    }
}
