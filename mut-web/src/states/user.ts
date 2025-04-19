import type {UserType} from "../models/user";

let currentUser: UserType;

const setCurrentUserStates = (user: UserType) => {
    currentUser = user;
}

const getCurrentUserStates = () => {
    return currentUser;
}

export {
    setCurrentUserStates,
    getCurrentUserStates,
}