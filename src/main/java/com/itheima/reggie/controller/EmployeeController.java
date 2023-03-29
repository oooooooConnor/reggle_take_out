package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(value = "/employee")
public class EmployeeController {

    // @Autowired  不推荐
    @Resource
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping(value = "/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 后续还是要改成规范 -- 既在控制器中不能出现业务相关的代码，而应该封装在service
        // 1.获取前端传过来的用户password，并进行md5加密
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        // 2.根据用户username查询数据库
        Employee emp = employeeService.getOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employee.getUsername()));
        if (emp != null && password.equals(emp.getPassword()) && emp.getStatus() == 1) {
            request.getSession().setAttribute("employee", emp.getId());
            return R.success(emp);
        } else {
            return R.error("登录失败");
        }
//        // 感觉下面这样写不太好，因为除了这些验证可能还有其他意想不到的错误
//        // 所以用那个唯一验证正确的条件判断比较好
//        // 3.判读查询结果是否为空
//        if (emp == null) {
//            return R.error("登录失败");
//        }
//        // 4.判断密码是否正确
//        if (!password.equals(emp.getPassword())) {
//            return R.error("登录失败");
//        }
//        // 5.查看用户状态是否禁用
//        if (emp.getStatus() == 0) {
//            return R.error("账号已禁用");
//        }
//        // 登录成功，将员工id存入Session并返回登录成功结果
//        request.getSession().setAttribute("employee", emp.getId());
//        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        // 设置初始密码“123456”，使用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping(value = "/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据ID修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        log.info(employee.toString());
        return null;
    }
}
