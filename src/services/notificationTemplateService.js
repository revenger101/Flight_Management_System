import apiClient from '../api/apiClient';

const BASE = '/notifications/templates';

export const notificationTemplateService = {
  getTemplates: () => apiClient.get(BASE),
  upsertTemplate: (payload) => apiClient.post(BASE, payload),
};
