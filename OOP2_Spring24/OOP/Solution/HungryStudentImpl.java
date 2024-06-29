package OOP.Solution;

import java.util.*;
import OOP.Provided.*;

public class HungryStudentImpl implements Comparable<HungryStudentImpl> {
    private int m_id;
    private String m_name;
    private Set<RestaurantImpl> m_favoriteRestaurants;
    private Set<HungryStudentImpl> m_friends;

    public HungryStudentImpl(int id, String name) {
        this.m_id = id;
        this.m_name = name;
        this.m_favoriteRestaurants = new HashSet<>();
        this.m_friends = new HashSet<>();
    }

    public HungryStudentImpl favorite(RestaurantImpl restaurant) throws UnratedFavoriteRestaurantException {
        if (!restaurant.getRatings().containsKey(this)) {
            throw new UnratedFavoriteRestaurantException();
        }
        this.m_favoriteRestaurants.add(restaurant);
        return this;
    }

    public Set<RestaurantImpl> favorites() {
        return new HashSet<>(this.m_favoriteRestaurants);
    }

    public HungryStudentImpl addFriend(HungryStudentImpl s) throws SameStudentException, ConnectionAlreadyExistsException {
        if (this.equals(s)) {
            throw new SameStudentException();
        }
        if (this.m_friends.contains(s)) {
            throw new ConnectionAlreadyExistsException();
        }
        this.m_friends.add(s);
        s.m_friends.add(this); // Ensuring bidirectional friendship
        return this;
    }

    public Set<HungryStudentImpl> getFriends() {
        return new HashSet<>(this.m_friends);
    }

    public Set<RestaurantImpl> favoritesByRating(int r) {
        // TreeSet is implemented with element sorting by default
        Set<RestaurantImpl> result = new TreeSet<>(new Comparator<RestaurantImpl>() { // TreeSet orders it automatically
            @Override
            public int compare(RestaurantImpl r1, RestaurantImpl r2) {
                int comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                return r1.compareTo(r2);
            }
        });
        for (RestaurantImpl restaurant : this.m_favoriteRestaurants) {
            if (restaurant.averageRating() >= r) {
                result.add(restaurant);
            }
        }
        return result;
    }

    public Set<RestaurantImpl> favoritesByDist(int r) {
        Set<RestaurantImpl> result = new TreeSet<>(new Comparator<RestaurantImpl>() {
            @Override
            public int compare(RestaurantImpl r1, RestaurantImpl r2) {
                int comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                return r1.compareTo(r2);
            }
        });
        for (RestaurantImpl restaurant : this.m_favoriteRestaurants) {
            if (restaurant.distance() <= r) {
                result.add(restaurant);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HungryStudentImpl that = (HungryStudentImpl) o;
        return m_id == that.m_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id);
    }

    @Override
    public int compareTo(HungryStudentImpl other) {
        return Integer.compare(this.m_id, other.m_id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hungry student: ").append(this.m_name).append(".\n");
        sb.append("Id: ").append(this.m_id).append(".\n");
        sb.append("Favorites: ");

        List<String> sortedFavorites = new ArrayList<>();
        for (RestaurantImpl restaurant : this.m_favoriteRestaurants) {
            sortedFavorites.add(restaurant.getName());
        }
        Collections.sort(sortedFavorites);

        for (int i = 0; i < sortedFavorites.size(); i++) {
            sb.append(sortedFavorites.get(i));
            if (i < sortedFavorites.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(".");
        return sb.toString();
    }
}

class UnratedFavoriteRestaurantException extends Exception {}
class SameStudentException extends Exception {}
class ConnectionAlreadyExistsException extends Exception {}
