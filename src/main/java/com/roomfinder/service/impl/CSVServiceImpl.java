package com.roomfinder.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.roomfinder.entity.Booking;
import com.roomfinder.entity.Message;
import com.roomfinder.entity.Room;
import com.roomfinder.entity.User;
import com.roomfinder.enums.BookingStatus;
import com.roomfinder.enums.UserRole;
import com.roomfinder.repository.BookingRepository;
import com.roomfinder.repository.MessageRepository;
import com.roomfinder.repository.RoomRepository;
import com.roomfinder.repository.UserRepository;
import com.roomfinder.service.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CSVServiceImpl implements CSVService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;



    @Autowired
    public CSVServiceImpl(UserRepository userRepository,
                          RoomRepository roomRepository,
                          MessageRepository messageRepository,
                          BookingRepository bookingRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ----- USER CSV OPERATIONS -----
    @Override
    public ByteArrayInputStream exportUsersToCSV() throws IOException {
        List<User> users = userRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            // Header row
            String[] header = {"id", "username", "password", "email", "fullName", "phoneNumber", "role", "createdAt", "updatedAt", "isActive"};
            writer.writeNext(header);

            for (User user : users) {
                String[] data = {
                        user.getId() != null ? String.valueOf(user.getId()) : "",
                        user.getUsername(),
                        user.getPassword(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhoneNumber(),
                        user.getRole() != null ? user.getRole().toString() : "",
                        user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
                        user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "",
                        String.valueOf(user.isActive())
                };
                writer.writeNext(data);
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importUsersFromCSV(MultipartFile file) throws IOException, CsvValidationException {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] nextRecord;
            boolean firstLine = true;
            int lineNumber = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                try {
                    User user = new User();
                    user.setUsername(nextRecord[1]);
                    String encryptedPassword = passwordEncoder.encode(nextRecord[2]);
                    user.setPassword(encryptedPassword);
                    user.setEmail(nextRecord[3]);
                    user.setFullName(nextRecord[4]);
                    user.setPhoneNumber(nextRecord[5]);
                    if (!nextRecord[6].isEmpty()) {
                        user.setRole(UserRole.valueOf(nextRecord[6]));
                    }
                    if (!nextRecord[7].isEmpty()) {
                        user.setCreatedAt(LocalDateTime.parse(nextRecord[7]));
                    }
                    if (!nextRecord[8].isEmpty()) {
                        user.setUpdatedAt(LocalDateTime.parse(nextRecord[8]));
                    }
                    user.setActive(Boolean.parseBoolean(nextRecord[9]));
                    users.add(user);
                } catch (Exception e) {
                    throw new RuntimeException("Error processing user at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        userRepository.saveAll(users);
    }

    // ----- ROOM CSV OPERATIONS -----
    @Override
    public ByteArrayInputStream exportRoomsToCSV() throws IOException {
        List<Room> rooms = roomRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            String[] header = {"id", "landlordId", "title", "description", "price", "address", "city", "size", "images", "isAvailable", "postedDate", "amenities"};
            writer.writeNext(header);

            for (Room room : rooms) {
                String imagesStr = room.getImages() != null ? String.join("|", room.getImages()) : "";
                String amenitiesStr = "";
                if (room.getAmenities() != null) {
                    amenitiesStr = room.getAmenities().entrySet().stream()
                            .map(e -> e.getKey() + ":" + e.getValue())
                            .collect(Collectors.joining("|"));
                }
                String[] data = {
                        room.getId() != null ? String.valueOf(room.getId()) : "",
                        room.getLandlordId() != null ? String.valueOf(room.getLandlordId()) : "",
                        room.getTitle(),
                        room.getDescription() != null ? room.getDescription() : "",
                        String.valueOf(room.getPrice()),
                        room.getAddress(),
                        room.getCity(),
                        String.valueOf(room.getSize()),
                        imagesStr,
                        String.valueOf(room.isAvailable()),
                        room.getPostedDate() != null ? room.getPostedDate().toString() : "",
                        amenitiesStr
                };
                writer.writeNext(data);
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public void importRoomsFromCSV(MultipartFile file) throws IOException {
        List<Room> rooms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] nextRecord;
            boolean firstLine = true;

            while ((nextRecord = csvReader.readNext()) != null) {
                if (firstLine) { // skip header
                    firstLine = false;
                    continue;
                }
                Room room = new Room();
                // Ignore id (auto-generated)
                room.setLandlordId(Long.parseLong(nextRecord[1]));
                room.setTitle(nextRecord[2]);
                room.setDescription(nextRecord[3]);
                room.setPrice(Double.parseDouble(nextRecord[4]));
                room.setAddress(nextRecord[5]);
                room.setCity(nextRecord[6]);
                room.setSize(Integer.parseInt(nextRecord[7]));
                // Images: split by "|" if not empty.
                String imagesStr = nextRecord[8];
                if (!imagesStr.isEmpty()) {
                    List<String> images = Arrays.asList(imagesStr.split("\\|"));
                    room.setImages(images);
                }
                room.setAvailable(Boolean.parseBoolean(nextRecord[9]));
                if (!nextRecord[10].isEmpty()) {
                    room.setPostedDate(LocalDateTime.parse(nextRecord[10]));
                }
                // Amenities: parse key:value pairs separated by "|"
                String amenitiesStr = nextRecord[11];
                if (!amenitiesStr.isEmpty()) {
                    Map<String, String> amenities = new HashMap<>();
                    String[] pairs = amenitiesStr.split("\\|");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            amenities.put(keyValue[0], keyValue[1]);
                        }
                    }
                    room.setAmenities(amenities);
                }
                rooms.add(room);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        roomRepository.saveAll(rooms);
    }

    // ----- MESSAGE CSV OPERATIONS -----
    @Override
    public ByteArrayInputStream exportMessagesToCSV() throws IOException {
        List<Message> messages = messageRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            String[] header = {"id", "senderId", "receiverId", "content", "sentAt", "isRead", "roomId"};
            writer.writeNext(header);

            for (Message message : messages) {
                String[] data = {
                        message.getId() != null ? String.valueOf(message.getId()) : "",
                        String.valueOf(message.getSenderId()),
                        String.valueOf(message.getReceiverId()),
                        message.getContent(),
                        message.getSentAt() != null ? message.getSentAt().toString() : "",
                        String.valueOf(message.isRead()),
                        message.getRoomId() != null ? String.valueOf(message.getRoomId()) : ""
                };
                writer.writeNext(data);
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importMessagesFromCSV(MultipartFile file) throws IOException {
        List<Message> messages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] nextRecord;
            boolean firstLine = true;
            int lineNumber = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                try {
                    Message message = new Message();
                    message.setSenderId(Long.parseLong(nextRecord[1]));
                    message.setReceiverId(Long.parseLong(nextRecord[2]));
                    message.setContent(nextRecord[3]);
                    if (!nextRecord[4].isEmpty()) {
                        message.setSentAt(LocalDateTime.parse(nextRecord[4]));
                    }
                    message.setRead(Boolean.parseBoolean(nextRecord[5]));
                    if (nextRecord.length > 6 && !nextRecord[6].isEmpty()) {
                        message.setRoomId(Long.parseLong(nextRecord[6]));
                    }
                    messages.add(message);
                } catch (Exception e) {
                    throw new RuntimeException("Error processing message at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException("CSV validation error: " + e.getMessage(), e);
        }
        messageRepository.saveAll(messages);
    }
    // ----- BOOKING CSV OPERATIONS -----
    @Override
    public ByteArrayInputStream exportBookingsToCSV() throws IOException {
        List<Booking> bookings = bookingRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            String[] header = {"id", "roomId", "seekerId", "bookingDate", "startDate", "endDate", "status", "comments"};
            writer.writeNext(header);

            for (Booking booking : bookings) {
                String[] data = {
                        booking.getId() != null ? String.valueOf(booking.getId()) : "",
                        String.valueOf(booking.getRoomId()),
                        String.valueOf(booking.getSeekerId()),
                        booking.getBookingDate() != null ? booking.getBookingDate().toString() : "",
                        booking.getStartDate() != null ? booking.getStartDate().toString() : "",
                        booking.getEndDate() != null ? booking.getEndDate().toString() : "",
                        booking.getStatus() != null ? booking.getStatus().toString() : "",
                        booking.getComments() != null ? booking.getComments() : ""
                };
                writer.writeNext(data);
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importBookingsFromCSV(MultipartFile file) throws IOException {
        List<Booking> bookings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] nextRecord;
            boolean firstLine = true;
            int lineNumber = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                try {
                    Booking booking = new Booking();
                    booking.setRoomId(Long.parseLong(nextRecord[1]));
                    booking.setSeekerId(Long.parseLong(nextRecord[2]));
                    if (!nextRecord[3].isEmpty()) {
                        booking.setBookingDate(LocalDateTime.parse(nextRecord[3]));
                    }
                    booking.setStartDate(LocalDate.parse(nextRecord[4]));
                    booking.setEndDate(LocalDate.parse(nextRecord[5]));
                    if (!nextRecord[6].isEmpty()) {
                        booking.setStatus(BookingStatus.valueOf(nextRecord[6]));
                    }
                    booking.setComments(nextRecord[7]);
                    bookings.add(booking);
                } catch (Exception e) {
                    throw new RuntimeException("Error processing booking at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException("CSV validation error: " + e.getMessage(), e);
        }
        bookingRepository.saveAll(bookings);
    }
}
