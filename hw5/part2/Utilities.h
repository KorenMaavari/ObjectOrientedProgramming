#ifndef OOP5_UTILITIES_H
#define OOP5_UTILITIES_H

template <bool B, typename T, typename F>
struct Conditional {
    typedef T value;
};

template <typename T, typename F>
struct Conditional<false, T, F> {
    typedef F value;
};

template<bool B, int T, int F>
struct ConditionalInteger {
    static constexpr int value = T;
};

template<int T, int F>
struct ConditionalInteger<false, T, F> {
    static constexpr int value = F;
};

#endif // OOP5_UTILITIES_H
