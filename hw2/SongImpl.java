package OOP.Solution;

import OOP.Provided.Song;
import OOP.Provided.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Song interface.
 */
public class SongImpl implements Song {
    private final int songID;
    private final String songName;
    private final int length; // Song length in seconds
    private final String singerName;
    private final Map<User, Integer> ratings; // Map to store users and their ratings

    public SongImpl(int songID, String songName, int length, String singerName) {
        if (songID < 0 || length < 0 || songName == null || singerName == null) {
            throw new IllegalArgumentException("Invalid parameters for SongImpl constructor.");
        }
        this.songID = songID;
        this.songName = songName;
        this.length = length;
        this.singerName = singerName;
        this.ratings = new HashMap<>();
    }

    @Override
    public int getID() {
        return songID;
    }

    @Override
    public String getName() {
        return songName;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getSingerName() {
        return singerName;
    }

    @Override
    public void rateSong(User user, int rate) throws User.IllegalRateValue, User.SongAlreadyRated {
        if (rate < 0 || rate > 10) {
            throw new User.IllegalRateValue();
        }
        if (ratings.containsKey(user)) {
            throw new User.SongAlreadyRated();
        }
        ratings.put(user, rate);
    }

    @Override
    public Collection<User> getRaters() {
        return ratings.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<User, Integer> entry) -> entry.getValue()).reversed() // Sort by rating descending
                        .thenComparing(entry -> entry.getKey().getAge()) // Then by age ascending
                        .thenComparing(entry -> -entry.getKey().getID())) // Then by ID descending
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Set<User>> getRatings() {
        return ratings.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue, // Group by rating
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet()) // Map users to a set
                ));
    }

    @Override
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return songID == song.getID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(songID);
    }

    @Override
    public int compareTo(Song other) {
        return Integer.compare(this.songID, other.getID());
    }
}
