import adminRequest from "../../utils/adminRequest.js";

//上传图片
export const uploadImageService = (formData) => {
  return adminRequest.post('/upload', formData);
}