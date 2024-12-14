package ru.itmo.cs.service;

import org.springframework.stereotype.Service;

@Service
public class CalculateDistanceService {
    public double calculate(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (Double.isNaN(x1) || Double.isNaN(y1) || Double.isNaN(z1) || Double.isNaN(x2) || Double.isNaN(y2) || Double.isNaN(z2)) {
            throw new IllegalArgumentException("Координаты не должны содержать NaN");
        }
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }
}
