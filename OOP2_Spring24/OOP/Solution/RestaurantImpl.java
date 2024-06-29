package OOP.Solution;

import java.util.*;
import OOP.Provided.*;

public class RestaurantImpl implements Comparable<RestaurantImpl> {
    private int m_id;
    private String m_name;
    private int m_distanceFromTech;
    private Set<String> m_menu;
    private Map<HungryStudentImpl, Integer> m_ratings; // Map to store student ratings

    public RestaurantImpl(int id, String name, int distFromTech, Set<String> menu) {
        this.m_id = id;
        this.m_name = name;
        this.m_distanceFromTech = distFromTech;
        this.m_menu = new HashSet<>(menu); // Koren: check if it is correct
        this.m_ratings = new HashMap<>();
    }

    public int distance() {
        return this.m_distanceFromTech;
    }

    public RestaurantImpl rate(HungryStudentImpl s, int r) throws RateRangeException {
        if (r < 0 || r > 5) {
            throw new RateRangeException();
        }
        this.m_ratings.put(s,r);
        return this;
    }

    public int numberOfRates() {
        return this.m_ratings.size();
    }

    public double averageRating() {
        int n = this.numberOfRates();
        if (n == 0)
            return 0.0;
        double sum = 0;
        for (int rating : this.m_ratings.values()) {
            sum += rating;
        }
        return sum / n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantImpl that = (RestaurantImpl) o;
        return this.m_id == that.m_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id);
    }

    @Override
    public int compareTo(RestaurantImpl other) {
        return Integer.compare(this.m_id, other.m_id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Restaurant: ").append(this.m_name).append(".\n");
        sb.append("Id: ").append(this.m_id).append(".\n");
        sb.append("Distance: ").append(this.m_distanceFromTech).append(".\n");
        sb.append("Menu: ");

        List<String> sortedMenu = new ArrayList<>(this.m_menu);
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

    public Map<HungryStudentImpl, Integer> getRatings() {
        return new HashMap<>(this.m_ratings);
    }

    public String getName() {
        return this.m_name;
    }
}

class RateRangeException extends Exception {}
