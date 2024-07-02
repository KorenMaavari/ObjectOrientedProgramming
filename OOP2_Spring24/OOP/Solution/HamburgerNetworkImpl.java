package OOP.Solution;

import java.util.*;

public class HamburgerNetworkImpl {
    private Map<Integer, HungryStudentImpl> m_students;
    private Map<Integer, RestaurantImpl> m_restaurants;

    public HamburgerNetworkImpl() {
        this.m_students = new HashMap<>();
        this.m_restaurants = new HashMap<>();
    }

    public HungryStudentImpl joinNetwork(int id, String name) throws StudentAlreadyInSystemException {
        if (this.m_students.containsKey(id)) {
            throw new StudentAlreadyInSystemException();
        }
        HungryStudentImpl student = new HungryStudentImpl(id, name);
        this.m_students.put(id, student);
        return student;
    }

    public RestaurantImpl addRestaurant (int id, String name, int dist, Set<String> menu) throws RestaurantAlreadyInSystemException {
        if (this.m_restaurants.containsKey(id)) {
            throw new RestaurantAlreadyInSystemException();
        }
        RestaurantImpl restaurant = new RestaurantImpl(id, name, dist, menu);
        this.m_restaurants.put(id, restaurant);
        return restaurant;
    }

    public Collection<HungryStudentImpl> registeredStudents() { // Koren: check if it is correct (the issue of the Collection)
        return this.m_students.values();
    }

    public Collection<RestaurantImpl> registeredRestaurants() { // Koren: check if it is correct (the issue of the Collection)
        return this.m_restaurants.values();
    }

    public HungryStudentImpl getStudent(int id) throws StudentNotInSystemException {
        HungryStudentImpl student = this.m_students.get(id);
        if (student == null) {
            throw new StudentNotInSystemException();
        }
        return student;
    }

    public RestaurantImpl getRestaurant(int id) throws RestaurantNotInSystemException {
        RestaurantImpl restaurant = this.m_restaurants.get(id);
        if (restaurant == null) {
            throw new RestaurantNotInSystemException();
        }
        return restaurant;
    }

    public HamburgerNetworkImpl addConnection (HungryStudentImpl s1, HungryStudentImpl s2) throws StudentNotInSystemException, SameStudentException, ConnectionAlreadyExistsException {
        if (!this.m_students.containsValue(s1) || !this.m_students.containsValue(s2)) {
            throw new StudentNotInSystemException();
        }
        s1.addFriend(s2);
        return this;
    }

    public Set<RestaurantImpl> favoritesByRating (HungryStudentImpl s) throws StudentNotInSystemException {
        if (!this.m_students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }
        Set<RestaurantImpl> result = new TreeSet<>(/*new Comparator<RestaurantImpl>() {
            @Override
            public int compare(RestaurantImpl r1, RestaurantImpl r2) {
                int comp = Double.compare(r2.averageRating(), r1.averageRating());
                if (comp != 0) return comp;
                comp = Integer.compare(r1.distance(), r2.distance());
                if (comp != 0) return comp;
                return r1.compareTo(r2);
            }
        }*/);
        // Convert to list
        List<HungryStudentImpl> list = new ArrayList<>(s.getFriends());
        // Sort the list by name
        list.sort(Comparator.comparing(HungryStudentImpl::getID));
        // Iterate over the sorted list
        for (HungryStudentImpl friend : list) {
            result.addAll(friend.favoritesByRating(0));
        }
        return result;
    }

    public Set<RestaurantImpl> favoritesByDist (HungryStudentImpl s) throws StudentNotInSystemException {
        if (!this.m_students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }
        Set<RestaurantImpl> result = new TreeSet<>(/**/);
        // Convert to list
        List<HungryStudentImpl> list = new ArrayList<>(s.getFriends());
        // Sort the list by name
        list.sort(Comparator.comparing(HungryStudentImpl::getID));
        // Iterate over the sorted list
        for (HungryStudentImpl friend : list) {
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
            HungryStudentImpl student = m_students.get(studentId);
            List<Integer> friendIds = new ArrayList<>();
            for (HungryStudentImpl friend : student.getFriends()) {
                friendIds.add(friend.getID());
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

    public boolean getRecommendationAux (HungryStudentImpl s, RestaurantImpl r, int t) {

    }

    public boolean getRecommendation(HungryStudentImpl s, RestaurantImpl r, int t) throws StudentNotInSystemException, RestaurantNotInSystemException, ImpossibleConnectionException {
        if (!this.m_students.containsValue(s)) {
            throw new StudentNotInSystemException();
        }
        if (!this.m_restaurants.containsValue(r)) {
            throw new RestaurantNotInSystemException();
        }
        if (t < 0) {
            throw new ImpossibleConnectionException();
        }
        // Koren: Is it supposed to be BFS or DFS or something else?
        Queue<HungryStudentImpl> queue = new LinkedList<>();
        Set<HungryStudentImpl> visited = new HashSet<>();
        queue.add(s);
        visited.add(s);
        int distance = 0;
        while (!queue.isEmpty() && distance <= t) {
            // Koren: We should continue from here
            HungryStudentImpl current = queue.poll();
        }
    }
}

class StudentAlreadyInSystemException extends Exception {}
class RestaurantAlreadyInSystemException extends Exception {}
class StudentNotInSystemException extends Exception {}
class RestaurantNotInSystemException extends Exception {}
class ImpossibleConnectionException extends Exception {}
