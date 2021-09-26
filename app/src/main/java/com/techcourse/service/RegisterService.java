package com.techcourse.service;

import com.techcourse.controller.LoginController;
import com.techcourse.controller.request.RegisterRequest;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.DuplicateAccountException;
import nextstep.web.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RegisterService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private final UserDao userDao;

    public RegisterService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void registerUser(RegisterRequest request) {
        User user = request.toEntity();
        validateDuplicate(user);

        userDao.insert(user);
    }

    private void validateDuplicate(User user) {
        if (userDao.findByAccount(user.getAccount()).isPresent()) {
            LOG.debug("Duplicate account already exist => {}", user.getAccount());
            throw new DuplicateAccountException();
        }
    }
}
