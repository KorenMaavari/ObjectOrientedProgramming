package OOP.Solution;

import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;

public class RestaurantImpl implements Restaurant {
    private int id;
    private String name;
    private int distance;
    private Set<String> menu;
    private Map<HungryStudent, Integer> ratings;

    public RestaurantImpl(int id, String name, int distance, Set<String> menu) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.menu = new HashSet<>(menu);
        this.ratings = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int distance() {
        return this.distance;
    }

    public Set<String> getMenu() {
        return new HashSet<>(this.menu);
    }

    @Override
    public Restaurant rate(HungryStudent s, int rating) throws RateRangeException {
        if (rating < 0 || rating > 5) {
            throw new RateRangeException();
        }
        this.ratings.put(s, rating);
        return this;
    }

    public Map<HungryStudent, Integer> getRatings() {
        return new HashMap<>(this.ratings);
    }

    @Override
    public int numberOfRates() {
        return this.ratings.size();
    }

    @Override
    public double averageRating() {
        if (this.ratings.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (int rating : this.ratings.values()) {
            sum += rating;
        }
        return sum / this.ratings.size();
    }

    public boolean didStudentRated(HungryStudent student) {
        return this.ratings.containsKey(student);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RestaurantImpl)) {
            return false;
        }
        RestaurantImpl otherRestaurant = (RestaurantImpl) o;
        return this.id == otherRestaurant.id;
    }

    @Override
    public int compareTo(Restaurant o) {
        return Integer.compare(this.id, ((RestaurantImpl)o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, distance, menu, ratings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Restaurant: ").append(this.name).append(".\n");
        sb.append("Id: ").append(this.id).append(".\n");
        sb.append("Distance: ").append(this.distance).append(".\n");
        sb.append("Menu: ");

        List<String> sortedMenu = new ArrayList<>(this.menu);
        Collections.sort(sortedMenu);

        for (int i = 0; i < sortedMenu.size(); i++) {
            sb.append(sortedMenu.get(i));
            if (i < sortedMenu.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(".");
        return sb.toString();
    }
}
