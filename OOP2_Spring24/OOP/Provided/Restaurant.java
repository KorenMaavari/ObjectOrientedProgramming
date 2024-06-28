package OOP.Provided;

/**
 * Each instance of the Restaurant class has an id, a name, distance
 * from the Technion, and holds a collection of menu items.
 * The id is unique for every restaurant.
 * */
public interface Restaurant extends Comparable<Restaurant> {

    class RestaurantAlreadyInSystemException    extends Exception {}
    class RestaurantNotInSystemException        extends Exception {}
    class RateRangeException                    extends Exception {}

    /**
     * @return the distance from the Technion.*/
    int distance();

    /**
     * rate the restaurant by a student
     * @return the object to allow concatenation of function calls.
     * @param s - the student rating the restaurant
     * @param r - the rating
     * */
    Restaurant rate(HungryStudent s, int r)
            throws RateRangeException;

    /**
     * @return the number of rating the restaurant has received
     * */
    int numberOfRates();

    /**
     * @return the restaurant's average rating
     * */
    double averageRating();

    /**
     * @return the restaurant's description as a string in the following format:
     * <format>
     * Restaurant: <name>.
     * Id: <id>.
     * Distance: <dist>.
     * Menu: <menuItem1, menuItem2, menuItem3...>.
     * </format>
     * Note: Menu items are ordered by lexicographical order, asc.
     *
     * Example:
     *
     * Restaurant: BBB.
     * Id: 1.
     * Distance: 5.
     * Menu: Cola, French Fries, Steak.
     *
     * */

    String toString();
}
