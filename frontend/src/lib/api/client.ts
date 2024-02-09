import axios from "axios";
import { refreshAuth } from ".";

export const client = axios.create({
  baseURL: "http://localhost:9000",
});

client.interceptors.response.use(
  (res) => res,
  async (error) => {
    const userId = window.localStorage.getItem("userId");
    const originalReq = error.config;

    if (error.response.status === 403 && userId && !originalReq._retried) {
      originalReq._retried = true;

      await refreshAuth(userId);

      return client(originalReq);
    }

    return Promise.reject(error);
  }
);
