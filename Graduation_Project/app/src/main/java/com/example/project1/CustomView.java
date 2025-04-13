package com.example.project1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CustomView extends View {
    private Paint paint;
    private Paint pointPaint;
    private List<Point> points;

    // Firebase
    private DatabaseReference databaseReference;

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(10);
        pointPaint.setStyle(Paint.Style.FILL);

        points = new ArrayList<>();

        // Initialize Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("coordinates").child("points");
    }

    public void addPoint(float x, float y) {
        if (points.size() < 4) {
            points.add(new Point(Math.round(x), Math.round(y)));
            invalidate();
        }
    }

    public void clearPoints() {
        points.clear();
        invalidate();
        clearPointsInFirebase();
    }

    public void uploadPointsToFirebase() {
        StringBuilder pointsString = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            pointsString.append(point.x).append(",").append(point.y);
            if (i < points.size() - 1) {
                pointsString.append(";");
            }
        }
        databaseReference.setValue(pointsString.toString());
    }

    public void clearPointsInFirebase() {
        databaseReference.setValue(null); // Hoặc sử dụng removeValue() nếu muốn xóa hoàn toàn
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Point point : points) {
            canvas.drawCircle(point.x, point.y, 10, pointPaint);
        }

        if (points.size() == 4) {
            for (int i = 0; i < points.size(); i++) {
                Point startPoint = points.get(i);
                Point endPoint = points.get((i + 1) % points.size());
                canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint);
            }

            // Upload points to Firebase when drawing is completed
            uploadPointsToFirebase();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                addPoint(x, y);
                return true;
            default:
                return false;
        }
    }

    // Model class for Firebase
    public static class Point {
        public int x;
        public int y;

        public Point() {
            // Default constructor required for calls to DataSnapshot.getValue(Point.class)
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
