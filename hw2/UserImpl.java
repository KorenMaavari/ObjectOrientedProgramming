package OOP.Solution;

import OOP.Provided.Song;
import OOP.Provided.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the User interface.
 */
public class UserImpl implements User {
    private final int userID;
    private final String userName;
    private final int userAge;
    private final Map<Song, Integer> ratedSongs; // Map to store songs and their ratings
    private final Set<User> friends; // Set of friends

    public UserImpl(int userID, String userName, int userAge) {
        if (userID < 0 || userAge < 0 || userName == null) {
            throw new IllegalArgumentException("Invalid parameters for UserImpl constructor");
        }
        this.userID = userID;
        this.userName = userName;
        this.userAge = userAge;
        this.ratedSongs = new HashMap<>();
        this.friends = new HashSet<>();
    }

    @Override
    public int getID() {
        return userID;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public int getAge() {
        return userAge;
    }

    @Override
    public User rateSong(Song song, int rate) throws IllegalRateValue, SongAlreadyRated {
        if (rate < 0 || rate > 10) {
            throw new IllegalRateValue();
        }
        if (ratedSongs.containsKey(song)) {
            throw new SongAlreadyRated();
        }
        ratedSongs.put(song, rate);
        return this;
    }

    @Override
    public double getAverageRating() {
        if (ratedSongs.isEmpty()) {
            return 0.0;
        }
        return ratedSongs.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    @Override
    public int getPlaylistLength() {
        return ratedSongs.keySet().stream().mapToInt(Song::getLength).sum();
    }

    @Override
    public Collection<Song> getRatedSongs() {
        return ratedSongs.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Song, Integer> entry) -> entry.getValue()).reversed()
                        .thenComparing(entry -> entry.getKey().getLength())
                        .thenComparing(entry -> -entry.getKey().getID())) // Sort by ID descending
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Song> getFavoriteSongs() {
        return ratedSongs.entrySet().stream()
                .filter(entry -> entry.getValue() >= 8)
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getID())) // Sort by ID ascending
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public User AddFriend(User friend) throws AlreadyFriends, SamePerson {
        if (this.equals(friend)) {
            throw new SamePerson();
        }
        if (!friends.add(friend)) {
            throw new AlreadyFriends();
        }
        return this;
    }

    @Override
    public boolean favoriteSongInCommon(User user) {
        if (!friends.contains(user)) {
            return false;
        }
        return this.getFavoriteSongs().stream().anyMatch(user.getFavoriteSongs()::contains);
    }

    @Override
    public Map<User, Integer> getFriends() {
        return friends.stream()
                .collect(Collectors.toMap(
                        friend -> friend,
                        friend -> friend.getRatedSongs().size()
                ));
    }

    @Override
    public int compareTo(User other) {
        return Integer.compare(this.userID, other.getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return this.userID == user.getID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
}
