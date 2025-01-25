#ifndef OOP5_SUBJECT_H
#define OOP5_SUBJECT_H

#include "OOP5EventException.h" // Include exception definitions
#include "Observer.h"
#include <vector>
#include <algorithm>

template <typename T>
class Subject {
    public:
        Subject() = default;

        void notify(const T& message) {
            for (Observer<T>* observer : m_observers) {
                observer->handleEvent(message);
            }
        }

        void addObserver(Observer<T>& observer) {
            if (count(m_observers.begin(),m_observers.end(), &observer) > 0) {
                throw ObserverAlreadyKnownToSubject();
            }
            m_observers.push_back(&observer);
        }

        void removeObserver(Observer<T>& observer) {
            if (!(count(m_observers.begin(),m_observers.end(), &observer) > 0)) {
                throw ObserverUnknownToSubject();
            }
            m_observers.erase(std::find(m_observers.begin(),m_observers.end(), &observer));
        }

        Subject<T>& operator+= (Observer<T>& observer) {
            addObserver(observer);
            return *this;
        }

        Subject<T>& operator-= (Observer<T>& observer) {
            removeObserver(observer);
            return *this;
        }

        Subject<T>& operator()(const T& message) {
            notify(message);
            return *this;
        }

    protected:
        std::vector<Observer<T>*> m_observers;
};

#endif // OOP5_SUBJECT_H
