package OOP.Solution;

import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;
import java.util.stream.Collectors;

public class HungryStudentImpl implements HungryStudent {
    private int id; // Unique identifier for the student
    private String name; // Name of the student
    private List<Restaurant> favoriteRestaurants; // List of favorite restaurants
    private Map<Integer, HungryStudent> friends; // Map of friends by their IDs

    // Constructor to initialize the student with ID and name
    public HungryStudentImpl(int id, String name) {
        this.id = id;
        this.name = name;
        this.favoriteRestaurants = new ArrayList<>();
        this.friends = new TreeMap<>();
    }

    @Override
    public HungryStudent favorite(Restaurant r) throws UnratedFavoriteRestaurantException {
        // Ensure the restaurant has been rated by this student before adding to favorites
        if (r instanceof RestaurantImpl) {
            RestaurantImpl convertedR = (RestaurantImpl) r;
            if (convertedR.didStudentRated(this)) {
                if (!favoriteRestaurants.contains(r)) {
                    favoriteRestaurants.add(r);
                }
                return this;
            }
        }
        throw new UnratedFavoriteRestaurantException();
    }

    @Override
    public Collection<Restaurant> favorites() {
        // Return a copy of the favorite restaurants list
        return new ArrayList<>(this.favoriteRestaurants);
    }

    @Override
    public HungryStudent addFriend(HungryStudent s) throws SameStudentException, ConnectionAlreadyExistsException {
        // Ensure the student is not adding themselves and that the connection does not already exist
        if (this.equals(s)) {
            throw new SameStudentException();
        }
        HungryStudentImpl student = (HungryStudentImpl) s;
        if (this.friends.containsKey(student.id)) {
            throw new ConnectionAlreadyExistsException();
        }
        this.friends.put(student.id, s);
        student.friends.put(this.id, this); // Ensure bidirectional friendship
        return this;
    }

    @Override
    public Set<HungryStudent> getFriends() {
        // Return a sorted set of friends by their IDs
        return new TreeSet<>(this.friends.values());
    }

    @Override
    public Collection<Restaurant> favoritesByRating(int rLimit) {
        // Sort the favorite restaurants by rating, then distance, then ID
        List<Restaurant> sortedList = new ArrayList<>(favoriteRestaurants);
        sortedList.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                RestaurantImpl restaurantR1 = (RestaurantImpl) r1;
                RestaurantImpl restaurantR2 = (RestaurantImpl) r2;
                // Compare by rating in descending order
                int comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) {
                    return comp;
                }
                // If ratings are equal, compare by distance in ascending order
                comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) {
                    return comp;
                }
                // If distances are also equal, compare by ID in ascending order
                return Integer.compare(restaurantR1.getId(), restaurantR2.getId());
            }
        });
        // Filter restaurants by the given rating limit
        return sortedList.stream().filter(restaurant -> restaurant.averageRating() >= rLimit).collect(Collectors.toList());
    }

    @Override
    public Collection<Restaurant> favoritesByDist(int dLimit) {
        // Sort the favorite restaurants by distance, then rating, then ID
        List<Restaurant> sortedList = new ArrayList<>(favoriteRestaurants);
        sortedList.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                RestaurantImpl restaurantR1 = (RestaurantImpl) r1;
                RestaurantImpl restaurantR2 = (RestaurantImpl) r2;
                // Compare by distance in ascending order
                int comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) {
                    return comp;
                }
                // If distances are equal, compare by rating in descending order
                comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) {
                    return comp;
                }
                // If ratings are also equal, compare by ID in ascending order
                return Integer.compare(restaurantR1.getId(), restaurantR2.getId());
            }
        });
        // Filter restaurants by the given distance limit
        return sortedList.stream().filter(restaurant -> restaurant.distance() <= dLimit).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HungryStudentImpl)) {
            return false;
        }
        HungryStudentImpl otherStudent = (HungryStudentImpl) o;
        return (otherStudent.id == this.id);
    }

    @Override
    public int compareTo(HungryStudent o) {
        if (o instanceof HungryStudentImpl) {
            HungryStudentImpl otherStudent = (HungryStudentImpl) o;
            return Integer.compare(this.id, otherStudent.id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        Set<String> stringFavoriteRestaurants = new TreeSet<>();
        for (Restaurant r : this.favoriteRestaurants) {
            if (r instanceof RestaurantImpl) {
                RestaurantImpl restaurant = (RestaurantImpl) r;
                stringFavoriteRestaurants.add(restaurant.getName());
            }
        }
        String favoritesToString = String.join(", ", stringFavoriteRestaurants);
        return String.format("Hungry student: %s.\nId: %d.\nFavorites: %s.",
                this.name, this.id, favoritesToString);
    }

    // Getter method for the student ID
    public int getId() {
        return this.id;
    }
}
