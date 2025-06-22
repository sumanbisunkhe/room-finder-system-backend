package com.roomfinder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long landlordId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private int size;

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    @Column(name = "is_available")
    private boolean available = true;

    private LocalDateTime postedDate = LocalDateTime.now();

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @MapKeyColumn(name = "amenity_name")
    @Column(name = "amenity_value")
    private Map<String, String> amenities = new HashMap<>();

}