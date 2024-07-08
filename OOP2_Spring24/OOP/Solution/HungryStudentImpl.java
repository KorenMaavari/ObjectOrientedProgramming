package OOP.Solution;

import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;
import java.util.stream.Collectors;

public class HungryStudentImpl implements HungryStudent {
    private int m_id; // Unique identifier for the student
    private String m_name; // Name of the student
    private List<Restaurant> m_favoriteRestaurants; // List of favorite restaurants
    private Map<Integer, HungryStudent> m_friends; // Map of friends by their IDs

    // Constructor to initialize the student with ID and name
    public HungryStudentImpl(int id, String name) {
        this.m_id = id;
        this.m_name = name;
        this.m_favoriteRestaurants = new ArrayList<>();
        this.m_friends = new TreeMap<>();
    }

    @Override
    public HungryStudent favorite(Restaurant r) throws UnratedFavoriteRestaurantException {
        // Ensure the restaurant has been rated by this student before adding to favorites
        if (r instanceof RestaurantImpl) {
            RestaurantImpl convertedR = (RestaurantImpl) r;
            if (convertedR.didStudentRate(this)) {
                if (!m_favoriteRestaurants.contains(r)) {
                    m_favoriteRestaurants.add(r);
                }
                return this;
            }
        }
        throw new UnratedFavoriteRestaurantException();
    }

    @Override
    public Collection<Restaurant> favorites() {
        // Return a copy of the favorite restaurants list
        return new ArrayList<>(this.m_favoriteRestaurants);
    }

    @Override
    public HungryStudent addFriend(HungryStudent s) throws SameStudentException, ConnectionAlreadyExistsException {
        // Ensure the student is not adding themselves and that the connection does not already exist
        if (this.equals(s)) {
            throw new SameStudentException();
        }
        HungryStudentImpl student = (HungryStudentImpl) s;
        if (this.m_friends.containsKey(student.m_id)) {
            throw new ConnectionAlreadyExistsException();
        }
        this.m_friends.put(student.m_id, s);
        return this;
    }

    @Override
    public Set<HungryStudent> getFriends() {
        // Return a sorted set of friends by their IDs
        Set<HungryStudent> copyOfFriends = new TreeSet<>(new Comparator<HungryStudent>() {
            @Override
            public int compare(HungryStudent o1, HungryStudent o2) {
                return Integer.compare(((HungryStudentImpl) o1).getID(), ((HungryStudentImpl) o2).getID());
            }
        });
        copyOfFriends.addAll(this.m_friends.values());
        return copyOfFriends;
    }

    @Override
    public Collection<Restaurant> favoritesByRating(int rLimit) {
        // Sort the favorite restaurants by rating, then distance, then ID
        List<Restaurant> sortedList = new ArrayList<>(m_favoriteRestaurants);
        sortedList.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                RestaurantImpl restaurantR1 = (RestaurantImpl) r1;
                RestaurantImpl restaurantR2 = (RestaurantImpl) r2;
                // Compare by rating in descending order
                int ratingCompare = Double.compare(r2.averageRating(), r1.averageRating());
                if (ratingCompare != 0) {
                    return ratingCompare;
                }
                // If ratings are equal, compare by distance in ascending order
                int distanceCompare = Integer.compare(r1.distance(), r2.distance());
                if (distanceCompare != 0) {
                    return distanceCompare;
                }
                // If distances are also equal, compare by ID in ascending order
                return Integer.compare(restaurantR1.getID(), restaurantR2.getID());
            }
        });
        // Filter restaurants by the given rating limit
        return sortedList.stream().filter(restaurant -> restaurant.averageRating() >= rLimit).collect(Collectors.toList());
    }

    @Override
    public Collection<Restaurant> favoritesByDist(int dLimit) {
        // Sort the favorite restaurants by distance, then rating, then ID
        List<Restaurant> sortedList = new ArrayList<>(m_favoriteRestaurants);
        sortedList.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                RestaurantImpl restaurantR1 = (RestaurantImpl) r1;
                RestaurantImpl restaurantR2 = (RestaurantImpl) r2;
                // Compare by distance in ascending order
                int distanceCompare = Integer.compare(r1.distance(), r2.distance());
                if (distanceCompare != 0) {
                    return distanceCompare;
                }
                // If distances are equal, compare by rating in descending order
                int ratingCompare = Double.compare(r2.averageRating(), r1.averageRating());
                if (ratingCompare != 0) {
                    return ratingCompare;
                }
                // If ratings are also equal, compare by ID in ascending order
                return Integer.compare(restaurantR1.getID(), restaurantR2.getID());
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
        return (otherStudent.m_id == this.m_id);
    }

    @Override
    public int compareTo(HungryStudent o) {
        if (o instanceof HungryStudentImpl) {
            HungryStudentImpl otherStudent = (HungryStudentImpl) o;
            return Integer.compare(this.m_id, otherStudent.m_id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int hashCode() {
        return this.m_id;
    }

    @Override
    public String toString() {
        Set<String> stringFavoriteRestaurants = new TreeSet<>();
        for (Restaurant r : this.m_favoriteRestaurants) {
            if (r instanceof RestaurantImpl) {
                RestaurantImpl restaurant = (RestaurantImpl) r;
                stringFavoriteRestaurants.add(restaurant.getName());
            }
        }
        String favoritesToString = String.join(", ", stringFavoriteRestaurants);
        return String.format("Hungry student: %s.\nId: %d.\nFavorites: %s.",
                this.m_name, this.m_id, favoritesToString);
    }

    // Getter method for the student ID
    int getID() {
        return this.m_id;
    }
}
