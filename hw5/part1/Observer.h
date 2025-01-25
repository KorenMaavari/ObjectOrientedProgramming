#ifndef OOP5_OBSERVER_H
#define OOP5_OBSERVER_H

template <typename T>
class Observer {
    public:
    Observer() = default;
    virtual ~Observer() = default;

    virtual void handleEvent(const T& t) = 0;
};

#endif // OOP5_OBSERVER_H