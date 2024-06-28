package OOP.Provided;

import java.util.Collection;
import java.util.Set;

/**
 * Each instance of the HungryStudent class has an id, a name, and holds
 * a collection of favorite restaurants, and a collection of student friends.
 * The id is unique for every student.
 * */
public interface HungryStudent extends Comparable<HungryStudent> {

    class SameStudentException                  extends Exception {}
    class StudentAlreadyInSystemException       extends Exception {}
    class StudentNotInSystemException           extends Exception {}
    class UnratedFavoriteRestaurantException    extends Exception {}
    class ConnectionAlreadyExistsException      extends Exception {}

    /**
     * the student favorites a restaurant
     *
     * @return the object to allow concatenation of function calls.
     * @param r - the restaurant being favored by the student
     * */
    HungryStudent favorite(Restaurant r)
            throws UnratedFavoriteRestaurantException;

    /**
     * @return the student's favorite restaurants
     * */
    Collection<Restaurant> favorites();

    /**
     * adding a student as a friend
     * @return the object to allow concatenation of function calls.
     * @param s - the student being "friend-ed"
     * */
    HungryStudent addFriend(HungryStudent s)
            throws SameStudentException, ConnectionAlreadyExistsException;

    /**
     * @return the student's set of friends
     * */
    Set<HungryStudent> getFriends();

    /**
     * @return the student's favorite restaurants, ordered by rating.
     * @param rLimit - the limit of rating under which restaurants will not be included.
     * */
    Collection<Restaurant> favoritesByRating(int rLimit);

    /**
     * @return the student's favorite restaurants, ordered by distance and then rating.
     * @param dLimit - the limit of distance above which restaurants will not be included.
     * */
    Collection<Restaurant> favoritesByDist(int dLimit);

    /**
     * @return the students's description as a string in the following format:
     * <format>
     * Hungry student: <name>.
     * Id: <id>.
     * Favorites: <resName1, resName2, resName3...>
     * </format>
     * Note: favorite restaurants are ordered by lexicographical order, asc.
     *
     * Example:
     *
     * Hungry student: Oren.
     * Id: 236703.
     * Favorites: BBB, Burger salon.
     *
     * */

    String toString();
}
