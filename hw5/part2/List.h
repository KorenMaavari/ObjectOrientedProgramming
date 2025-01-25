#ifndef OOP5_LIST_H
#define OOP5_LIST_H

template <typename... TT>
struct List {};

template <typename T, typename... Ts>
struct List <T, Ts...> {
    typedef T head;
    typedef List<Ts...> next;
    static constexpr int size = (sizeof...(Ts)) + 1;
};

// TODO: Ask in Piazza what if the list is empty.
 template <typename T>
 struct List<T> {
     typedef T head;
     typedef List<> next;
     static constexpr int size = 1;
 };

template <>
struct List<> {
    static constexpr int size = 0;
};

//TODO: Ask in Reception Hour if we can do something like that:

template<typename T, typename L>
struct PrependList{};

template<typename T, typename... Ts>
struct PrependList<T, List<Ts...>> {
    typedef List<T, Ts...> list;
};

template<int N, typename L>
struct GetAtIndex{};

template<int N, typename T, typename... Ts>
struct GetAtIndex<N, List<T, Ts...>> {
    typedef typename GetAtIndex<N - 1, List<Ts...>>::value value;
};

template<typename T, typename... Ts>
struct GetAtIndex<0, List<T, Ts...>> {
    typedef T value;
};

template<int N, typename U, typename L>
struct SetAtIndex{};

template<int N, typename U, typename T, typename... Ts>
struct SetAtIndex<N, U, List<T, Ts...>> {
    typedef typename PrependList<T, typename SetAtIndex<N - 1, U, List<Ts...>>::list>::list list;
};

template<typename U, typename T, typename... Ts>
struct SetAtIndex<0, U, List<T, Ts...>> {
    typedef List<U, Ts...> list;
};

#endif // OOP5_LIST_H
