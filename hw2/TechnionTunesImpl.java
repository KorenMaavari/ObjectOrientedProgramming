package OOP.Solution;

import OOP.Provided.Song;
import OOP.Provided.TechnionTunes;
import OOP.Provided.User;
import OOP.Provided.User.*;

import java.util.*;

import static java.lang.Math.signum;

public class TechnionTunesImpl implements TechnionTunes {

    private final Map<Integer, User> users; // Map to store users by ID
    private final Map<Integer, Song> songs; // Map to store songs by ID

    public TechnionTunesImpl() {
        this.users = new HashMap<>();
        this.songs = new HashMap<>();
    }

    /**
     * Adds a new user to the system. If the user already exists, a UserAlreadyExists
     * exception is thrown.
     *
     * @param userID   User ID
     * @param userName User name
     * @param userAge  User age
     * @throws UserAlreadyExists if a user with the same ID already exists
     */
    @Override
    public void addUser(int userID, String userName, int userAge) throws UserAlreadyExists {
        if (users.containsKey(userID)) {
            throw new UserAlreadyExists();
        }
        if (userName == null || userAge < 0) {
            throw new IllegalArgumentException("Invalid user details");
        }
        users.put(userID, new UserImpl(userID, userName, userAge));
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id User ID
     * @return a reference to the user
     * @throws UserDoesntExist if there is no user with the specified ID
     */
    @Override
    public User getUser(int id) throws UserDoesntExist {
        User user = users.get(id);
        if (user == null) {
            throw new UserDoesntExist();
        }
        return user;
    }

    /**
     * Establishes a friendship between two users.
     *
     * @param id1 User ID of the first user
     * @param id2 User ID of the second user
     * @throws AlreadyFriends  if the users are already friends
     * @throws UserDoesntExist if one or both users don't exist
     * @throws SamePerson      if id1 and id2 are the same
     */
    @Override
    public void makeFriends(int id1, int id2) throws UserDoesntExist, AlreadyFriends, SamePerson {
        User user1 = getUser(id1); // Throws UserDoesntExist if user1 doesn't exist
        User user2 = getUser(id2); // Throws UserDoesntExist if user2 doesn't exist
        user1.AddFriend(user2);
        user2.AddFriend(user1); // Friendship is bidirectional
    }

    /**
     * Adds a new song to the system.
     *
     * @param songID     Song ID
     * @param songName   Song name
     * @param length     Song length in seconds
     * @param singerName Singer name
     * @throws SongAlreadyExists if a song with the same ID already exists
     */
    @Override
    public void addSong(int songID, String songName, int length, String singerName) throws SongAlreadyExists {
        if (songs.containsKey(songID)) {
            throw new SongAlreadyExists();
        }
        if (songName == null || singerName == null || length <= 0) {
            throw new IllegalArgumentException("Invalid song details");
        }
        songs.put(songID, new SongImpl(songID, songName, length, singerName));
    }

    /**
     * Retrieves a song by ID.
     *
     * @param id Song ID
     * @return a reference to the song
     * @throws SongDoesntExist if there is no song with the specified ID
     */
    @Override
    public Song getSong(int id) throws SongDoesntExist {
        Song song = songs.get(id);
        if (song == null) {
            throw new SongDoesntExist();
        }
        return song;
    }

    /**
     * Allows a user to rate a song.
     *
     * @param userId User ID
     * @param songId Song ID
     * @param rate   Rating value
     * @throws UserDoesntExist if the user doesn't exist
     * @throws SongDoesntExist if the song doesn't exist
     * @throws SongAlreadyRated if the user has already rated the song
     * @throws IllegalRateValue if the rate value is out of range
     */
    @Override
    public void rateSong(int userId, int songId, int rate) throws UserDoesntExist, SongDoesntExist, IllegalRateValue, SongAlreadyRated {
        User user = getUser(userId);
        Song song = getSong(songId);
        user.rateSong(song, rate);
        song.rateSong(user, rate);
    }

    /**
     * Returns the intersection of rated songs among a list of users.
     *
     * @param IDs Array of user IDs
     * @return a set of songs rated by all the given users
     * @throws UserDoesntExist if one or more users don't exist
     */
    @Override
    public Set<Song> getIntersection(int[] IDs) throws UserDoesntExist {
        if (IDs == null || IDs.length == 0) {
            return Collections.emptySet();
        }
        Set<Song> intersection = new HashSet<>(getUser(IDs[0]).getRatedSongs());
        for (int id : IDs) {
            intersection.retainAll(getUser(id).getRatedSongs());
        }
        return intersection;
    }

    /**
     * Returns a collection of songs sorted according to the provided comparator.
     *
     * @param comp Comparator for sorting songs
     * @return a sorted collection of songs
     */
    @Override
    public Collection<Song> sortSongs(Comparator<Song> comp) {
        return songs.values().stream()
                .sorted(comp)
                .toList();
    }

    /**
     * Returns the top highest-rated songs.
     *
     * @param num Number of songs to return
     * @return a collection of the highest-rated songs
     */
    @Override
    public Collection<Song> getHighestRatedSongs(int num) {
        return songs.values().stream()
                .sorted(Comparator.comparingDouble(Song::getAverageRating)
                        .thenComparingInt(Song::getLength).reversed()
                        .thenComparingInt(Song::getID))
                .limit(num)
                .toList();
    }

    /**
     * Returns the most-rated songs.
     *
     * @param num Number of songs to return
     * @return a collection of the most-rated songs
     */
    @Override
    public Collection<Song> getMostRatedSongs(int num) {
        return songs.values().stream()
                .sorted(Comparator.comparingInt((Song s) -> s.getRaters().size()).reversed()
                        .thenComparingInt(Song::getLength)
                        .thenComparing(Comparator.comparingInt(Song::getID).reversed()))
                .limit(num)
                .toList();
    }

    /**
     * Returns the top likers (users with the highest average rating).
     *
     * @param num Number of users to return
     * @return a collection of the top likers
     */
    @Override
    public Collection<User> getTopLikers(int num) {
        return users.values().stream()
                .sorted(Comparator.comparingDouble(User::getAverageRating)
                        .thenComparingInt(User::getAge).reversed()
                        .thenComparingInt(User::getID))
                .limit(num)
                .toList();
    }

    /**
     * Determines if two users can get along based on the system's friendship graph.
     *
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @return true if the users can get along, false otherwise
     * @throws UserDoesntExist if one or both users don't exist
     */
    @Override
    public boolean canGetAlong(int userId1, int userId2) throws UserDoesntExist {
        User user1 = getUser(userId1);
        User user2 = getUser(userId2);
        if (user1.equals(user2)) {
            return true; // A user always gets along with themselves
        }

        // BFS to find a path in the friendship graph
        Set<User> visited = new HashSet<>();
        Queue<User> queue = new LinkedList<>();
        queue.add(user1);
        visited.add(user2);

        while (!queue.isEmpty()) {
            User current = queue.poll();
            if (current.equals(user2)) {
                return true;
            } else if (current.favoriteSongInCommon(user2)) {
                return true;
            }
            visited.add(current);
            for (User friend : current.getFriends().keySet()) {
                if (!visited.contains(friend) && current.favoriteSongInCommon(friend)) {
                    queue.add(friend);
                }
            }
        }
        return false;
    }

    /**
     * Returns an iterator over the songs in the system, sorted by length (ascending)
     * and then by ID (ascending).
     *
     * @return an iterator over the sorted songs
     */
    @Override
    public Iterator<Song> iterator() {
        return songs.values().stream()
                .sorted(Comparator.comparingInt(Song::getLength)
                        .thenComparingInt(Song::getID))
                .iterator();
    }
}
