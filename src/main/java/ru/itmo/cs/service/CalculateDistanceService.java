package ru.itmo.cs.service;

import org.springframework.stereotype.Service;

@Service
public class CalculateDistanceService {
    public double calculate(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }
}
