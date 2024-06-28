package OOP.Provided;

import java.util.Collection;
import OOP.Provided.HungryStudent.*;
import OOP.Provided.Restaurant.*;
import java.util.Set;

/**
 * Each instance of the HamburgerNetwork class has holds a collection of registered students,
 * a collection of registered restaurants, and manages different relations between
 * the two.
 * */
public interface HamburgerNetwork {

    class ImpossibleConnectionException extends Exception {}

    /**
     * add a hungry student to the network.
     *
     * @param id - the id of the student
     * @param name - the name of the student
     * @return the HungryStudent added
     * */
    HungryStudent joinNetwork(int id, String name)
            throws StudentAlreadyInSystemException;

    /**
     * add a restaurant to the network
     * @param id - the id of the restaurant
     * @param name - the name of the restaurant
     * @param dist - the distance of the restaurant from the Technion
     * @param menu - the set of menu items of the restaurant
     * @return the Restaurant added
     * */
    Restaurant addRestaurant(int id, String name, int dist, Set<String> menu)
            throws RestaurantAlreadyInSystemException;

    /**
     * @return a collection of all students in the network
     * */
    Collection<HungryStudent> registeredStudents();

    /**
     * @return a collection of all restaurants in the network
     * */
    Collection<Restaurant> registeredRestaurants();

    /**
     * @return the student in the network by the id given
     * @param id - the id of the student to look for in the network
     * */
    HungryStudent getStudent(int id)
            throws StudentNotInSystemException;

    /**
     * @return the restaurant in the network by the id given
     * @param id - the id of the restaurant to look for in the network
     * */
    Restaurant getRestaurant(int id)
            throws RestaurantNotInSystemException;

    /**
     * add a connection of friendship between the two students received.
     * friendship is a symmetric relation!
     *
     * @return the object to allow concatenation of function calls.
     * @param s1 - the first student
     * @param s2 - the second student
     * */
    HamburgerNetwork addConnection(HungryStudent s1, HungryStudent s2)
            throws StudentNotInSystemException, ConnectionAlreadyExistsException, SameStudentException;

    /**
     * returns a collection of restaurants favored by the friends of the received student,
     * ordered by rating
     *
     * @param s - the student whom in relation to him, favored restaurants by his friends are considered
     * */
    Collection<Restaurant> favoritesByRating(HungryStudent s)
            throws StudentNotInSystemException;

    /**
     * returns a collection of restaurants favored by the friends of the received student,
     * ordered by distance from the Technion
     *
     * @param s - the student whom in relation to him, favored restaurants by his friends are considered
     * */
    Collection<Restaurant> favoritesByDist(HungryStudent s)
            throws StudentNotInSystemException;

    /**
     * check whether the restaurant received is t-recommended by the received student (definition in the PDF)
     *
     * @param s - the student (dis)recommending the restaurant
     * @param r - the restaurant being (dis)recommended
     * @param t - the limit in the t-recommendation
     *
     * @return whether s t-recommends r
     * */
    boolean getRecommendation(HungryStudent s, Restaurant r, int t)
            throws StudentNotInSystemException, RestaurantNotInSystemException, ImpossibleConnectionException;

    /**
     * @return the network's description as a string in the following format:
     * <format>
     * Registered students: <studentId1, studentId2, studentId3...>.
     * Registered restaurants: <resId1, resId2, resId3...>.
     * Students:
     * <student1Id> -> [<friend1Id, friend2Id, friend3Id...>].
     * <student2Id> -> [<friend1Id, friend2Id, friend3Id...>].
     * ...
     * End students.
     * </format>
     * Note: students, restaurants and friends' ids are ordered by natural integer order, asc.*
     * Example:
     *
     * Registered students: 1, 236703, 555555.
     * Registered restaurants: 12, 13.
     * Students:
     * 1 -> [236703, 555555555].
     * 236703 -> [1].
     * 555555 -> [1].
     * End students.
     * */

    String toString();
}
