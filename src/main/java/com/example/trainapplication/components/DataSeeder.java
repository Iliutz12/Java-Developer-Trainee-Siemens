package com.example.trainapplication.components;

import com.example.trainapplication.model.Schedule;
import com.example.trainapplication.model.Station;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.repositories.IScheduleRepository;
import com.example.trainapplication.repositories.IStationRepository;
import com.example.trainapplication.repositories.ITrainRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.Objects;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ITrainRepository trainRepository;
    private final IStationRepository stationRepository;
    private final IScheduleRepository scheduleRepository;

    private static final String START_MARKER = "START";
    private static final String END_MARKER   = "END";
    private static final String CSV_PATH     = "/trains_initial_data.csv";

    public DataSeeder(ITrainRepository trainRepository,
                      IStationRepository stationRepository,
                      IScheduleRepository scheduleRepository) {
        this.trainRepository    = trainRepository;
        this.stationRepository  = stationRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (trainRepository.count() > 0) {
            System.out.println("[DataSeeder] Database already contains data — skipping seed.");
            return;
        }
        System.out.println("[DataSeeder] Populating database with initial train schedules...");
        loadCsvData();
        System.out.println("[DataSeeder] Database successfully populated!");
    }

    @Transactional
    public void loadCsvData() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(CSV_PATH),
                        "CSV file not found at: " + CSV_PATH
                )))) {

            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.isBlank()) continue;

                String[] data = line.split(",");
                if (data.length < 6) {
                    System.err.printf("[DataSeeder] Skipping malformed line %d: %s%n", lineNumber, line);
                    continue;
                }

                String  trainName     = data[0].trim();
                int     capacity      = Integer.parseInt(data[1].trim());
                String  stationName   = data[2].trim();
                String  arrivalStr    = data[3].trim();
                String  departureStr  = data[4].trim();
                int     stopOrder     = Integer.parseInt(data[5].trim());

                Station station = stationRepository.findByName(stationName);
                if (station == null) {
                    station = stationRepository.save(new Station(stationName));
                }

                Train train = trainRepository.findByName(trainName);
                if (train == null) {
                    train = new Train();
                    train.setName(trainName);
                    train.setTotalCapacity(capacity);
                    train.setDelayMinutes(0);
                    train = trainRepository.save(train);
                }

                LocalTime arrivalTime   = START_MARKER.equals(arrivalStr)   ? null : LocalTime.parse(arrivalStr);
                LocalTime departureTime = END_MARKER.equals(departureStr)   ? null : LocalTime.parse(departureStr);

                Schedule schedule = new Schedule();
                schedule.setTrain(train);
                schedule.setStation(station);
                schedule.setArrivalTime(arrivalTime);
                schedule.setDepartureTime(departureTime);
                schedule.setStopOrder(stopOrder);
                scheduleRepository.save(schedule);

            }
        } catch (Exception e) {
            System.err.println("[DataSeeder] Failed to load initial data: " + e.getMessage());
            throw new RuntimeException("DataSeeder failed — rolling back.", e);
        }
    }
}