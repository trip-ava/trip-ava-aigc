//package group.rxcloud.ava.aigc.operator;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class ResourceOperator {
//
//    String uploadPath = "D:\\aioverflow\\";
//
//    public void uploadImage(MultipartFile[] images) {
//
//        // 设置文件上传的目录
//        List<String> paths = new ArrayList<>();
//        try {
//            for (int i = 0; i < images.length; i++) {
//                MultipartFile image = images[i];
//                // 创建目标文件对象
//                String path = uploadPath + "upload_%d.png";
//                String pathname = String.format(path, i);
//                File destinationFile = new File(pathname);
//                // 保存文件到目标路径
//                image.transferTo(destinationFile);
//                paths.add(pathname);
//            }
//            AIGCRequest request = new AIGCRequest();
//            request.setImageUrls(paths);
//            String html = aigcava(request);
//            return html;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "文件上传失败";
//        }
//        return "http://localhost:5000/";
//    }
//}
