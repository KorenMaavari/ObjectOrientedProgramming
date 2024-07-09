package OOP.Solution;

import OOP.Provided.HamburgerNetwork;
import OOP.Provided.Restaurant;
import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant.RestaurantAlreadyInSystemException;
import OOP.Provided.Restaurant.RestaurantNotInSystemException;
import OOP.Provided.HungryStudent.SameStudentException;
import OOP.Provided.HungryStudent.ConnectionAlreadyExistsException;
import OOP.Provided.HungryStudent.StudentNotInSystemException;
import OOP.Provided.Restaurant.RateRangeException;
import OOP.Provided.HamburgerNetwork.ImpossibleConnectionException;

import java.util.*;

public class HamburgerNetworkImpl implements HamburgerNetwork {

    private Map<Integer, HungryStudent> students;
    private Map<Integer, Restaurant> restaurants;

    public HamburgerNetworkImpl() {
        students = new HashMap<>();
        restaurants = new HashMap<>();
    }

    @Override
    public HungryStudent joinNetwork(int id, String name) throws HungryStudent.StudentAlreadyInSystemException {
        if (students.containsKey(id)) {
            throw new HungryStudent.StudentAlreadyInSystemException();
        }
        HungryStudent newStudent = new HungryStudentImpl(id, name);
        students.put(id, newStudent);
        return newStudent;
    }

    @Override
    public Restaurant addRestaurant(int id, String name, int dist, Set<String> menu) throws RestaurantAlreadyInSystemException {
        if (restaurants.containsKey(id)) {
            throw new RestaurantAlreadyInSystemException();
        }
        Restaurant newRestaurant = new RestaurantImpl(id, name, dist, menu);
        restaurants.put(id, newRestaurant);
        return newRestaurant;
    }

    public Collection<Restaurant> getRestaurants() {
        return restaurants.values();
    }

    public Collection<HungryStudent> getStudents() {
        return students.values();
    }

    @Override
    public Restaurant getRestaurant(int id) throws RestaurantNotInSystemException {
        if (!restaurants.containsKey(id)) {
            throw new RestaurantNotInSystemException();
        }
        return restaurants.get(id);
    }

    @Override
    public HungryStudent getStudent(int id) throws StudentNotInSystemException {
        if (!students.containsKey(id)) {
            throw new StudentNotInSystemException();
        }
        return students.get(id);
    }

    public HamburgerNetwork addConnection(int id1, int id2) throws StudentNotInSystemException, ConnectionAlreadyExistsException, SameStudentException {
        if (!students.containsKey(id1) || !students.containsKey(id2)) {
            throw new StudentNotInSystemException();
        }
        HungryStudent s1 = students.get(id1);
        HungryStudent s2 = students.get(id2);
        s1.addFriend(s2);
        return this;
    }

    @Override
    public HamburgerNetwork addConnection(HungryStudent s1, HungryStudent s2) throws StudentNotInSystemException, ConnectionAlreadyExistsException, SameStudentException {
        if (!students.containsKey(((HungryStudentImpl)s1).getId()) || !students.containsKey(((HungryStudentImpl)s2).getId())) {
            throw new StudentNotInSystemException();
        }
        s1.addFriend(s2);
        return this;
    }

    @Override
    public Collection<Restaurant> favoritesByRating(HungryStudent s) throws StudentNotInSystemException {
        if (!students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }

        Set<Restaurant> result = new TreeSet<>(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                int comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                return Integer.compare(((RestaurantImpl)r1).getId(), ((RestaurantImpl)r2).getId());
            }
        });

        for (HungryStudent friend : s.getFriends()) {
            result.addAll(friend.favoritesByRating(0));
        }

        return result;
    }

    @Override
    public Collection<Restaurant> favoritesByDist(HungryStudent s) throws StudentNotInSystemException {
        if (!students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }

        Set<Restaurant> result = new TreeSet<>(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                int comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                return Integer.compare(((RestaurantImpl)r1).getId(), ((RestaurantImpl)r2).getId());
            }
        });

        for (HungryStudent friend : s.getFriends()) {
            result.addAll(friend.favoritesByDist(Integer.MAX_VALUE));
        }

        return result;
    }

    @Override
    public boolean getRecommendation(HungryStudent s, Restaurant r, int t) throws StudentNotInSystemException, RestaurantNotInSystemException, ImpossibleConnectionException {
        if (!students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }
        if (!restaurants.containsValue(r)) {
            throw new RestaurantNotInSystemException();
        }
        if (t < 0) {
            throw new ImpossibleConnectionException();
        }

        Queue<HungryStudent> queue = new LinkedList<>();
        Set<HungryStudent> visited = new HashSet<>();
        queue.add(s);
        visited.add(s);
        int distance = 0;

        while (!queue.isEmpty() && distance <= t) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                HungryStudent current = queue.poll();
                if (current.favorites().contains(r)) {
                    return true;
                }
                for (HungryStudent friend : current.getFriends()) {
                    if (!visited.contains(friend)) {
                        queue.add(friend);
                        visited.add(friend);
                    }
                }
            }
            distance++;
        }
        throw new ImpossibleConnectionException();
    }

    @Override
    public Collection<HungryStudent> registeredStudents() {
        // Returns a collection of all registered students
        return new ArrayList<>(students.values());
    }

    @Override
    public Collection<Restaurant> registeredRestaurants() {
        // Returns a collection of all registered restaurants
        return new ArrayList<>(restaurants.values());
    }
}

class StudentAlreadyInSystemException extends Exception {}