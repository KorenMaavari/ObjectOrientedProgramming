package OOP.Solution;

import java.util.*;
import OOP.Provided.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HamburgerNetworkImpl implements HamburgerNetwork {
    private Map<Integer, HungryStudent> m_students;
    private Map<Integer, Restaurant> m_restaurants;

    public HamburgerNetworkImpl() {
        this.m_students = new HashMap<>();
        this.m_restaurants = new HashMap<>();
    }

    public HungryStudentImpl joinNetwork(int id, String name) throws HungryStudent.StudentAlreadyInSystemException {
        if (this.m_students.containsKey(id)) {
            throw new HungryStudent.StudentAlreadyInSystemException();
        }
        HungryStudentImpl student = new HungryStudentImpl(id, name);
        this.m_students.put(id, student);
        return student;
    }

    public RestaurantImpl addRestaurant (int id, String name, int dist, Set<String> menu) throws Restaurant.RestaurantAlreadyInSystemException {
        if (this.m_restaurants.containsKey(id)) {
            throw new Restaurant.RestaurantAlreadyInSystemException();
        }
        RestaurantImpl restaurant = new RestaurantImpl(id, name, dist, menu);
        this.m_restaurants.put(id, restaurant);
        return restaurant;
    }

    public Collection<HungryStudent> registeredStudents() { // Koren: check if it is correct (the issue of the Collection)
        return this.m_students.values();
    }

    public Collection<Restaurant> registeredRestaurants() { // Koren: check if it is correct (the issue of the Collection)
        return this.m_restaurants.values();
    }

    public HungryStudentImpl getStudent(int id) throws HungryStudent.StudentNotInSystemException {
        HungryStudent student = this.m_students.get(id);
        if (student == null) {
            throw new HungryStudent.StudentNotInSystemException();
        }
        HungryStudentImpl convertedStudent = (HungryStudentImpl) student;
        return convertedStudent;
    }

    public RestaurantImpl getRestaurant(int id) throws Restaurant.RestaurantNotInSystemException {
        Restaurant restaurant = this.m_restaurants.get(id);
        if (restaurant == null) {
            throw new Restaurant.RestaurantNotInSystemException();
        }
        RestaurantImpl convertedRestaurant = (RestaurantImpl) restaurant;
        return convertedRestaurant;
    }

    public HamburgerNetworkImpl addConnection (HungryStudent s1, HungryStudent s2) throws HungryStudent.StudentNotInSystemException, HungryStudent.SameStudentException, HungryStudent.ConnectionAlreadyExistsException {
        if (!this.m_students.containsValue(s1) || !this.m_students.containsValue(s2)) {
            throw new HungryStudent.StudentNotInSystemException();
        }
        s1.addFriend(s2);
        return this;
    }

    public Set<Restaurant> favoritesByRating(HungryStudent s) throws HungryStudent.StudentNotInSystemException {
        if (!this.m_students.containsValue(s)) {
            throw new HungryStudent.StudentNotInSystemException();
        }
        Set<Restaurant> result = new TreeSet<>(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                int comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                return r1.compareTo(r2);
            }
        });
        // Traverse friends in ascending order of their IDs
        Set<HungryStudent> sortedFriends = new TreeSet<>(s.getFriends());
        for (HungryStudent friend : sortedFriends) {
            result.addAll(friend.favoritesByRating(0));
        }
        return result;
    }

    public Set<Restaurant> favoritesByDist (HungryStudent s) throws HungryStudent.StudentNotInSystemException {
        if (!this.m_students.containsValue(s)) {
            throw new HungryStudent.StudentNotInSystemException();
        }
        Set<Restaurant> result = new TreeSet<>(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                int comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                return r1.compareTo(r2);
            }
        });
        // Traverse friends in ascending order of their IDs
        Set<HungryStudent> sortedFriends = new TreeSet<>(s.getFriends());
        for (HungryStudent friend : sortedFriends) {
            result.addAll(friend.favoritesByDist(Integer.MAX_VALUE));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Registered students: ");

        List<Integer> studentIds = new ArrayList<>(m_students.keySet());
        Collections.sort(studentIds);
        for (int i = 0; i < studentIds.size(); i++) {
            sb.append(studentIds.get(i));
            if (i < studentIds.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(".\n");

        sb.append("Registered restaurants: ");
        List<Integer> restaurantIds = new ArrayList<>(m_restaurants.keySet());
        Collections.sort(restaurantIds);
        for (int i = 0; i < restaurantIds.size(); i++) {
            sb.append(restaurantIds.get(i));
            if (i < restaurantIds.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(".\n");

        sb.append("Students:\n");
        for (Integer studentId : studentIds) {
            sb.append(studentId).append(" -> [");
            HungryStudent student = m_students.get(studentId);
            List<Integer> friendIds = new ArrayList<>();
            for (HungryStudent friend : student.getFriends()) {
                HungryStudentImpl convertedStudent = (HungryStudentImpl) friend;
                friendIds.add(convertedStudent.getID());
            }
            Collections.sort(friendIds);
            for (int i = 0; i < friendIds.size(); i++) {
                sb.append(friendIds.get(i));
                if (i < friendIds.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("].\n");
        }
        sb.append("End students.");
        return sb.toString();
    }

    public boolean getRecommendation(HungryStudent s, Restaurant r, int t) throws HungryStudent.StudentNotInSystemException, Restaurant.RestaurantNotInSystemException, ImpossibleConnectionException {
        if (!this.m_students.containsValue(s)) {
            throw new HungryStudent.StudentNotInSystemException();
        }
        if (!this.m_restaurants.containsValue(r)) {
            throw new Restaurant.RestaurantNotInSystemException();
        }
        if (t < 0) {
            throw new ImpossibleConnectionException();
        }
        // Koren: Is it supposed to be BFS or DFS or something else?
        Queue<HungryStudentImpl> queue = new LinkedList<>();
        Set<HungryStudentImpl> visited = new HashSet<>();
        HungryStudentImpl convertedStudent = (HungryStudentImpl) s;
        queue.add(convertedStudent);
        visited.add(convertedStudent);
        int distance = 0;
        while (!queue.isEmpty() && distance <= t) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                HungryStudentImpl current = queue.poll(); // v := Q.dequeue()
                // if v is the goal then
                if (current.favorites().contains(r)) {
                    return true; // return v
                }
                // for all edges from v to w in G.adjacentEdges(v) do
                for (HungryStudent friend : current.getFriends()) {
                    if (!visited.contains(friend)) { // if w is not labeled as explored then
                        HungryStudentImpl convertedFriend = (HungryStudentImpl) friend;
                        queue.add(convertedFriend); // Q.enqueue(w)
                        visited.add(convertedFriend); // label w as explored
                    }
                }
            }
            distance++; // Increment the distance after processing each level of friends
        }
        return false; // The restaurant is not recommended within t distance
    }
}

class StudentAlreadyInSystemException extends Exception {}
class RestaurantAlreadyInSystemException extends Exception {}
class StudentNotInSystemException extends Exception {}
class RestaurantNotInSystemException extends Exception {}
class ImpossibleConnectionException extends Exception {}
