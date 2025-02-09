package com.roomfinder.service;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface CSVService {

    // User CSV operations
    ByteArrayInputStream exportUsersToCSV() throws IOException;
    void importUsersFromCSV(MultipartFile file) throws IOException, CsvValidationException;

    // Room CSV operations
    ByteArrayInputStream exportRoomsToCSV() throws IOException;
    void importRoomsFromCSV(MultipartFile file) throws IOException;

    // Message CSV operations
    ByteArrayInputStream exportMessagesToCSV() throws IOException;
    void importMessagesFromCSV(MultipartFile file) throws IOException;

    // Booking CSV operations
    ByteArrayInputStream exportBookingsToCSV() throws IOException;
    void importBookingsFromCSV(MultipartFile file) throws IOException;
}
