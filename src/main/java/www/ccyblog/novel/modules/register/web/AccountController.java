package www.ccyblog.novel.modules.register.web;

import com.octo.captcha.service.CaptchaServiceException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import www.ccyblog.novel.modules.register.service.AccountService;
import www.ccyblog.novel.modules.register.service.JCaptchaService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import www.ccyblog.novel.modules.register.service.AccountService.REGISTER_ERROR_INFO;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * Created by ccy on 2017/7/31.
 */
@Log4j
@Controller
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private JCaptchaService jCaptchaService;

    @RequestMapping(value = "/register", method = GET)
    public String registerForm(){
//        accountService.createAccount("mynameisxiaoxiao", "hchc08235");
        return "register";
    }

    @RequestMapping(value = "/register", method = POST)
    public String registerAccount(@RequestParam(value = "username",defaultValue = "") String username ,
                                  @RequestParam(value = "password",defaultValue = "") String password ,
                                  @RequestParam(value = "rePassword",defaultValue = "") String rePassword ,
                                  @RequestParam(value = "captcha",defaultValue = "") String captcha ,
                                  Model model){

        REGISTER_ERROR_INFO status =  accountService.createAccount(username, password, rePassword, captcha);
        switch (status){
            case NORMAL: return "home";
            case CAPTCHA: return "redirect:register?error=captcha";
            case USERNAME: return "redirect:register?error=username";
            case PASSWORD: return "redirect:register?error=password";
            case OTHER: return "redirect:register?error=other";
            default: return "redirect:register?error=other";
        }
    }

    @RequestMapping(value="/captcha.jpeg")
    public void getJCaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        byte[] captchaChallengeAsJpeg = null;
        // the output stream to render the captcha image as jpeg into
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            // get the session id that will identify the generated captcha.
            //the same id must be used to validate the response, the session id is a good candidate!
            String captchaId = httpServletRequest.getSession().getId();
            // call the ImageCaptchaService getChallenge method
            BufferedImage challenge =
                    jCaptchaService.getImageChallengeForID(captchaId,
                            httpServletRequest.getLocale());

            // a jpeg encoder

            JPEGImageEncoder jpegEncoder =
                    JPEGCodec.createJPEGEncoder(jpegOutputStream);
            jpegEncoder.encode(challenge);

        } catch (IllegalArgumentException e) {
            try {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        } catch (CaptchaServiceException e) {
            try {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

        // flush it in the response
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = null;
        try {
            responseOutputStream = httpServletResponse.getOutputStream();
            responseOutputStream.write(captchaChallengeAsJpeg);
            responseOutputStream.flush();
            responseOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/login", method = POST)
    public String login(String username, String password){
        return "redirect:";
    }

    //《sping in action》没有提示依赖哪些库，尝试一下午，终于确定是jackson-core 和 jackson-databind
    @RequestMapping(value="/query.json", method = POST)
    public @ResponseBody Map queryUsername(@RequestParam(value = "username") String username){
        boolean isRepeat = accountService.hasUsername(username);
        HashMap hashMap = new HashMap<String, Boolean>();
        hashMap.put("repeat", isRepeat);
        return hashMap;
    }
}
